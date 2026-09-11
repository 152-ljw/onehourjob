package com.onehourjob.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onehourjob.service.ApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/application")
@CrossOrigin
public class ApplicationController {

    private final ApplicationService applicationService;
    private final ObjectMapper objectMapper;

    public ApplicationController(ApplicationService applicationService, ObjectMapper objectMapper) {
        this.applicationService = applicationService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/apply")
    public Map<String, Object> apply(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String bodyStr = new String(request.getInputStream().readAllBytes(), "UTF-8");
            if (bodyStr == null || bodyStr.isBlank()) {
                result.put("success", false);
                result.put("message", "请求体不能为空");
                return result;
            }
            Map<String, Object> body = objectMapper.readValue(bodyStr, Map.class);
            Object jobIdObj = body.get("jobId");
            if (jobIdObj == null) {
                result.put("success", false);
                result.put("message", "岗位ID不能为空");
                return result;
            }
            long userId = (Long) request.getAttribute("userId");
            long jobId = Long.parseLong(String.valueOf(jobIdObj));
            return applicationService.apply(userId, jobId);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "请求参数格式错误，请确保发送的是JSON格式：{\"jobId\": 123}");
            return result;
        }
    }

    @GetMapping("/list")
    public Map<String, Object> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<Map<String, Object>> applications = applicationService.listByUserId(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("applications", applications);
        return result;
    }
}