package com.onehourjob.controller;

import com.onehourjob.service.JobKnowledgeService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin
public class AiKnowledgeController {

    private final JobKnowledgeService jobKnowledgeService;

    public AiKnowledgeController(JobKnowledgeService jobKnowledgeService) {
        this.jobKnowledgeService = jobKnowledgeService;
    }

    @GetMapping("/search")
    public Map<String, Object> search(@RequestParam(required = false) String keyword) {
        Map<String, Object> result = new HashMap<>();
        if (keyword == null || keyword.isBlank()) {
            result.put("success", false);
            result.put("message", "请输入搜索内容");
            return result;
        }
        List<JobKnowledgeService.SearchResult> list = jobKnowledgeService.search(keyword.trim());
        result.put("success", true);
        result.put("data", list);
        return result;
    }

    @PostMapping("/rag")
    public Map<String, Object> rag(@RequestBody(required = false) Map<String, String> body) {
        Map<String, Object> result = new HashMap<>();
        String message = body == null ? null : body.get("message");
        if (message == null || message.isBlank()) {
            result.put("success", false);
            result.put("message", "说点什么吧");
            return result;
        }
        String answer = jobKnowledgeService.ragAsk(message.trim());
        Map<String, String> data = new HashMap<>();
        data.put("answer", answer);
        result.put("success", true);
        result.put("data", data);
        return result;
    }
}