package com.onehourjob.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onehourjob.entity.Resume;
import com.onehourjob.service.ResumeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/resume")
@CrossOrigin
public class ResumeController {

    private final ResumeService resumeService;
    private final ObjectMapper objectMapper;

    public ResumeController(ResumeService resumeService, ObjectMapper objectMapper) {
        this.resumeService = resumeService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/save")
    public Map<String, Object> save(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String bodyStr = new String(request.getInputStream().readAllBytes(), "UTF-8");
            if (bodyStr == null || bodyStr.isBlank()) {
                result.put("success", false);
                result.put("message", "请求体不能为空");
                return result;
            }
            Map<String, Object> body = objectMapper.readValue(bodyStr, Map.class);
            Long userId = (Long) request.getAttribute("userId");
            Resume resume = new Resume();
            resume.setUserId(userId);
            resume.setContent((String) body.get("content"));
            return resumeService.save(resume);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "请求参数格式错误，请确保发送的是JSON格式：{\"content\": \"简历内容\"}");
            return result;
        }
    }

    @GetMapping("/get")
    public Map<String, Object> getByUserId(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Resume resume = resumeService.getByUserId(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        if (resume == null) {
            result.put("hasResume", false);
            result.put("resume", null);
        } else {
            result.put("hasResume", true);
            result.put("resume", resume);
        }
        return result;
    }
}