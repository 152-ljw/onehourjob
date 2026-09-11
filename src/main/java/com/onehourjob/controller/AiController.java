package com.onehourjob.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * AI 推荐接口：接收前端技能描述，转发到 Dify 工作流，返回 AI 推荐文字。
 * Dify 密钥只保存在后端，前端不感知 Dify 的存在。
 */
@RestController
@RequestMapping("/api/ai")
@CrossOrigin
public class AiController {

    private final String difyApiUrl;
    private final String difyApiKey;
    private final RestTemplate restTemplate;

    public AiController(@Value("${dify.api-url}") String difyApiUrl,
                        @Value("${dify.api-key}") String difyApiKey) {
        this.difyApiUrl = difyApiUrl;
        this.difyApiKey = difyApiKey;
        // AI 工作流耗时较长（约20秒），读超时设 60 秒
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(60000);
        this.restTemplate = new RestTemplate(factory);
    }

    /**
     * AI 岗位推荐
     * 请求体：{"skill": "用户填写的技能描述"}
     * 返回：{"success": true, "data": "AI推荐文字"}
     */
    @PostMapping("/recommend")
    public Map<String, Object> recommend(@RequestBody(required = false) Map<String, String> body) {
        Map<String, Object> result = new HashMap<>();
        String skill = body == null ? null : body.get("skill");
        if (skill == null || skill.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "请先填写技能");
            return result;
        }

        // 组装 Dify 工作流请求体：inputs.skill 对应工作流「开始」节点的输入变量
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("skill", skill.trim());
        Map<String, Object> reqBody = new HashMap<>();
        reqBody.put("inputs", inputs);
        reqBody.put("response_mode", "blocking");
        reqBody.put("user", "onehourjob-user");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(difyApiKey);

        try {
            ResponseEntity<Map> resp = restTemplate.exchange(
                    difyApiUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(reqBody, headers),
                    Map.class
            );
            String text = extractText(resp.getBody());
            if (text == null || text.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "AI 暂无推荐结果，请稍后再试");
            } else {
                result.put("success", true);
                // 去掉 qwen 深度思考模型输出的 <think>...</think> 思考过程，只返回正式回答
                result.put("data", stripThink(text));
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "AI 服务调用失败，请稍后再试");
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 从 Dify 返回结构中取出推荐文字：data.outputs.text
     */
    @SuppressWarnings("unchecked")
    private String extractText(Map<String, Object> respBody) {
        if (respBody == null) {
            return null;
        }
        Object data = respBody.get("data");
        if (data instanceof Map) {
            Object outputs = ((Map<String, Object>) data).get("outputs");
            if (outputs instanceof Map) {
                Object text = ((Map<String, Object>) outputs).get("text");
                return text == null ? null : text.toString();
            }
        }
        return null;
    }

    /**
     * 去除模型思考过程标签 <think>...</think>
     */
    private String stripThink(String text) {
        return text.replaceAll("(?s)<think>.*?</think>", "").trim();
    }
}
