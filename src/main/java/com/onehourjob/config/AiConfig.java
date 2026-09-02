package com.onehourjob.config;

import dev.langchain4j.model.dashscope.QwenEmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Value("${langchain4j.dashscope.api-key}")
    private String apiKey;

    @Value("${langchain4j.dashscope.embedding.model-name:text-embedding-v2}")
    private String modelName;

    @Bean
    public QwenEmbeddingModel qwenEmbeddingModel() { // 通义千问 Embedding 模型
        return QwenEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .build();
    }
}