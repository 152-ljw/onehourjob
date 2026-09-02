package com.onehourjob.controller;

import com.onehourjob.service.EmbeddingDemoService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/embed")
@CrossOrigin
public class EmbeddingDemoController {

    private final EmbeddingDemoService embeddingDemoService;

    public EmbeddingDemoController(EmbeddingDemoService embeddingDemoService) {
        this.embeddingDemoService = embeddingDemoService;
    }

    @GetMapping("/demo")
    public Map<String, Object> demo(@RequestParam(required = false) String text) { // text为空时返回友好提示
        if (text == null || text.isBlank()) {
            Map<String, Object> map = new HashMap<>();
            map.put("msg", "请传入 text 参数，例如：/api/embed/demo?text=我要找Java工作");
            return map;
        }
        return embeddingDemoService.compare(text);
    }
}