package com.onehourjob.service;

import com.onehourjob.entity.Job;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.dashscope.QwenChatModel;
import dev.langchain4j.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class JobKnowledgeService {

    private final QwenEmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final JobService jobService;

    @Value("${langchain4j.dashscope.api-key}")
    private String apiKey;

    @Value("${langchain4j.dashscope.chat.model-name:qwen-max}")
    private String chatModelName;

    private QwenChatModel chatModel;

    private static final Pattern JOB_ID_PATTERN = Pattern.compile("^岗位#(\\d+)");

    public JobKnowledgeService(QwenEmbeddingModel embeddingModel,
                               JobService jobService) {
        this.embeddingModel = embeddingModel;
        this.jobService = jobService;
        this.embeddingStore = new InMemoryEmbeddingStore<>();
    }

    @PostConstruct
    public void init() {
        try {
            List<Job> jobs = jobService.listByKeyword(null);
            if (jobs.isEmpty()) {
                log.info("岗位知识灌库完成，共0条");
                return;
            }
            List<TextSegment> segments = jobs.stream()
                    .map(this::toJobText)
                    .map(TextSegment::from)
                    .collect(Collectors.toList());
            List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
            embeddingStore.addAll(embeddings, segments);
            log.info("岗位知识灌库完成，共{}条", jobs.size());
        } catch (Exception e) {
            log.error("岗位知识灌库失败", e);
        }
    }

    private String toJobText(Job job) {
        return String.format("岗位#%d：%s｜%s｜%s｜%s｜%s",
                job.getId(),
                job.getTitle() != null ? job.getTitle() : "",
                job.getCompanyName() != null ? job.getCompanyName() : "",
                job.getSalary() != null ? job.getSalary() : "",
                job.getCategory() != null ? job.getCategory() : "",
                job.getRequirements() != null ? job.getRequirements() : "");
    }

    public List<SearchResult> search(String keyword) {
        Embedding queryEmbedding = embeddingModel.embed(keyword).content();
        EmbeddingSearchResult<TextSegment> result = embeddingStore.search(
                EmbeddingSearchRequest.builder()
                        .queryEmbedding(queryEmbedding)
                        .maxResults(5)
                        .build());
        return result.matches().stream()
                .map(this::toSearchResult)
                .sorted((a, b) -> Double.compare(b.score, a.score))
                .collect(Collectors.toList());
    }

    public String ragAsk(String question) {
        try {
            Embedding queryEmbedding = embeddingModel.embed(question).content();
            EmbeddingSearchResult<TextSegment> result = embeddingStore.search(
                    EmbeddingSearchRequest.builder()
                            .queryEmbedding(queryEmbedding)
                            .maxResults(3)
                            .build());
            List<EmbeddingMatch<TextSegment>> matches = result.matches();
            if (matches.isEmpty()) {
                return "知识库是空的，请重启后端重新灌库";
            }

            StringBuilder jobMaterial = new StringBuilder();
            List<Long> jobIds = new ArrayList<>();
            for (EmbeddingMatch<TextSegment> match : matches) {
                String text = match.embedded().text();
                jobMaterial.append(text).append("\n");
                Long jobId = parseJobId(text);
                if (jobId != null) {
                    jobIds.add(jobId);
                }
            }

            String systemMessage = "你是「一小时就业」平台的岗位顾问，回答必须基于提供的岗位资料，"
                    + "资料里没有的信息不要编造，回答末尾注明参考了哪几个岗位编号";
            String userMessage = "岗位资料：\n" + jobMaterial + "\n用户问题：" + question;

            List<ChatMessage> messages = List.of(
                    new SystemMessage(systemMessage),
                    new UserMessage(userMessage)
            );
            String answer = getChatModel().generate(messages).content().text();
            return answer;
        } catch (Exception e) {
            log.error("RAG 问答失败", e);
            return "AI 服务调用失败，请稍后再试：" + e.getMessage();
        }
    }

    private QwenChatModel getChatModel() {
        if (chatModel == null) {
            chatModel = QwenChatModel.builder()
                    .apiKey(apiKey)
                    .modelName(chatModelName)
                    .build();
        }
        return chatModel;
    }

    private SearchResult toSearchResult(EmbeddingMatch<TextSegment> match) {
        String text = match.embedded().text();
        return new SearchResult(match.score(), parseJobId(text), text);
    }

    private Long parseJobId(String text) {
        if (text == null) {
            return null;
        }
        Matcher matcher = JOB_ID_PATTERN.matcher(text);
        return matcher.find() ? Long.parseLong(matcher.group(1)) : null;
    }

    public record SearchResult(double score, Long jobId, String text) {}
}