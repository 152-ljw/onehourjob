package com.onehourjob.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onehourjob.entity.SysUser;
import com.onehourjob.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserController {

    private final UserService userService;
    private final ObjectMapper objectMapper;

    public UserController(UserService userService, ObjectMapper objectMapper) {
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody SysUser user) {
        return userService.register(user);
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody SysUser user) {
        return userService.login(user);
    }

    /**
     * 修改密码：请求体 {"oldPassword":"原密码","newPassword":"新密码"}，身份靠 token
     */
    @PostMapping("/password")
    public Map<String, Object> changePassword(HttpServletRequest request) {
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
            String oldPassword = (String) body.get("oldPassword");
            String newPassword = (String) body.get("newPassword");
            return userService.changePassword(userId, oldPassword, newPassword);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "请求参数格式错误");
            return result;
        }
    }

    @PostMapping("/profile")
    public Map<String, Object> updateProfile(HttpServletRequest request) {
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
            String nickname = (String) body.get("nickname");
            String avatar = (String) body.get("avatar");
            return userService.updateProfile(userId, nickname, avatar);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "请求参数格式错误，请确保发送的是JSON格式：{\"nickname\": \"昵称\", \"avatar\": \"头像URL\"}");
            return result;
        }
    }
}