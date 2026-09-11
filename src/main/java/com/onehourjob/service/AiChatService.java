package com.onehourjob.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.onehourjob.entity.ChatMessage;
import com.onehourjob.mapper.ChatMessageMapper;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.dashscope.QwenChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AiChatService {

    private final ChatMessageMapper chatMessageMapper;
    private final JobKnowledgeService jobKnowledgeService;

    @Value("${langchain4j.dashscope.api-key}")
    private String apiKey;

    @Value("${langchain4j.dashscope.chat.model-name:qwen-max}")
    private String chatModelName;

    private QwenChatModel chatModel;

    public AiChatService(ChatMessageMapper chatMessageMapper,
                         JobKnowledgeService jobKnowledgeService) {
        this.chatMessageMapper = chatMessageMapper;
        this.jobKnowledgeService = jobKnowledgeService;
    }

    public Map<String, Object> chat(Long userId, String userMessage) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<JobKnowledgeService.SearchResult> searchResults = jobKnowledgeService.search(userMessage);
            String jobMaterial = null;
            if (searchResults != null && !searchResults.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (JobKnowledgeService.SearchResult sr : searchResults) {
                    sb.append(sr.text()).append("\n");
                }
                jobMaterial = sb.toString();
            }

            LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ChatMessage::getUserId, userId)
                    .orderByDesc(ChatMessage::getId)
                    .last("LIMIT 8");
            List<ChatMessage> history = chatMessageMapper.selectList(wrapper);
            Collections.reverse(history);

            String historyText = null;
            if (!history.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (ChatMessage msg : history) {
                    if ("user".equals(msg.getRole())) {
                        sb.append("用户：").append(msg.getContent()).append("\n");
                    } else if ("assistant".equals(msg.getRole())) {
                        sb.append("助手：").append(msg.getContent()).append("\n");
                    }
                }
                historyText = sb.toString();
            }

            String systemMessage = "你是「一小时就业」平台的 AI 就业助手，帮应届生解答求职问题、"
                    + "介绍岗位、给简历建议，语气亲切专业；如果提供了岗位资料，介绍岗位时必须基于"
                    + "资料里的真实岗位，不要编造";

            StringBuilder userMsgBuilder = new StringBuilder();
            if (historyText != null) {
                userMsgBuilder.append("对话历史：\n").append(historyText).append("\n");
            }
            if (jobMaterial != null) {
                userMsgBuilder.append("岗位资料：\n").append(jobMaterial).append("\n");
            }
            userMsgBuilder.append("用户问题：").append(userMessage);

            String answer = getChatModel().generate(
                    List.of(new SystemMessage(systemMessage), new UserMessage(userMsgBuilder.toString()))
            ).content().text();

            ChatMessage userMsg = new ChatMessage();
            userMsg.setUserId(userId);
            userMsg.setRole("user");
            userMsg.setContent(userMessage);
            userMsg.setCreatedAt(LocalDateTime.now());
            chatMessageMapper.insert(userMsg);

            ChatMessage assistantMsg = new ChatMessage();
            assistantMsg.setUserId(userId);
            assistantMsg.setRole("assistant");
            assistantMsg.setContent(answer);
            assistantMsg.setCreatedAt(LocalDateTime.now());
            chatMessageMapper.insert(assistantMsg);

            Map<String, String> data = new HashMap<>();
            data.put("answer", answer);
            result.put("success", true);
            result.put("data", data);
        } catch (Exception e) {
            log.error("AI 对话失败", e);
            result.put("success", false);
            result.put("message", "AI 服务调用失败，请稍后再试：" + e.getMessage());
        }
        return result;
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
}