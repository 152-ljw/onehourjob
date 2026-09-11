package com.onehourjob.controller;

import com.onehourjob.service.AiChatService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin
public class AiChatController {

    private final AiChatService aiChatService;

    public AiChatController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody(required = false) Map<String, String> body,
                                    HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        String message = body == null ? null : body.get("message");
        if (message == null || message.isBlank()) {
            result.put("success", false);
            result.put("message", "说点什么吧");
            return result;
        }
        Long userId = (Long) request.getAttribute("userId");
        return aiChatService.chat(userId, message.trim());
    }
}