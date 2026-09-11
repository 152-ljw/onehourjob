package com.onehourjob.controller;

import com.onehourjob.service.AiResumeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/resume")
@CrossOrigin
public class AiResumeController {

    private final AiResumeService aiResumeService;

    public AiResumeController(AiResumeService aiResumeService) {
        this.aiResumeService = aiResumeService;
    }

    @PostMapping("/optimize")
    public Map<String, Object> optimize(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        Long userId = (Long) request.getAttribute("userId");
        String suggestion = aiResumeService.optimize(userId);
        if (suggestion == null) {
            result.put("success", true);
            result.put("message", "请先在【我的简历】里填写并保存简历");
            result.put("data", new HashMap<>());
            return result;
        }
        Map<String, String> data = new HashMap<>();
        data.put("suggestion", suggestion);
        result.put("success", true);
        result.put("data", data);
        return result;
    }
}