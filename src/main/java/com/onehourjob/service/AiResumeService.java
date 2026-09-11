package com.onehourjob.service;

import com.onehourjob.entity.Resume;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.dashscope.QwenChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class AiResumeService {

    private final ResumeService resumeService;

    @Value("${langchain4j.dashscope.api-key}")
    private String apiKey;

    @Value("${langchain4j.dashscope.chat.model-name:qwen-max}")
    private String chatModelName;

    private QwenChatModel chatModel;

    public AiResumeService(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    public String optimize(Long userId) {
        Resume resume = resumeService.getByUserId(userId);
        if (resume == null || resume.getContent() == null || resume.getContent().isBlank()) {
            return null;
        }

        String systemMessage = "你是资深 HR 简历优化专家。请对用户简历指出 3~4 个最主要的问题，"
                + "每条给出具体修改建议，最后用一句话做总结，语气专业友好";
        String userMessage = "这是我的简历内容，请帮我优化：\n" + resume.getContent();

        List<ChatMessage> messages = List.of(
                new SystemMessage(systemMessage),
                new UserMessage(userMessage)
        );
        String suggestion = getChatModel().generate(messages).content().text();
        return suggestion;
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