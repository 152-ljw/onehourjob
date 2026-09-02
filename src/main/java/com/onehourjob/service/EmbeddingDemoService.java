package com.onehourjob.service;

import dev.langchain4j.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.data.embedding.Embedding;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmbeddingDemoService {

    private final QwenEmbeddingModel qwenEmbeddingModel;

    private static final List<String> REFERENCE_WORDS = List.of(
            "Java后端开发", "Spring Boot工程师", "护士护理", "平面设计"
    );

    public EmbeddingDemoService(QwenEmbeddingModel qwenEmbeddingModel) {
        this.qwenEmbeddingModel = qwenEmbeddingModel;
    }

    public double cosineSimilarity(float[] a, float[] b) { // 计算两个向量的余弦相似度
        double dot = 0.0, normA = 0.0, normB = 0.0;
        for (int i = 0; i < a.length; i++) {
            dot += (double) a[i] * b[i];
            normA += (double) a[i] * a[i];
            normB += (double) b[i] * b[i];
        }
        if (normA == 0.0 || normB == 0.0) {
            return 0.0; // 零向量防护，直接返回0
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    public Map<String, Object> compare(String text) { // 将输入文本与参考词逐一比对
        Map<String, Object> result = new HashMap<>();

        Embedding inputEmbedding = qwenEmbeddingModel.embed(text).content(); // 输入文本向量化
        float[] inputVector = inputEmbedding.vector();

        List<Map<String, Object>> list = new ArrayList<>();
        for (String ref : REFERENCE_WORDS) {
            Embedding refEmbedding = qwenEmbeddingModel.embed(ref).content(); // 参考词向量化
            float[] refVector = refEmbedding.vector();
            double sim = cosineSimilarity(inputVector, refVector);
            double rounded = Math.round(sim * 100.0) / 100.0; // 保留两位小数
            Map<String, Object> item = new HashMap<>();
            item.put("reference", ref);
            item.put("similarity", rounded);
            list.add(item);
        }
        list.sort((o1, o2) -> Double.compare( // 按相似度从高到低排序
                (Double) o2.get("similarity"),
                (Double) o1.get("similarity")
        ));
        result.put("results", list);
        return result;
    }
}