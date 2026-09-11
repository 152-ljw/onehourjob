package com.onehourjob.controller;

import com.onehourjob.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin
public class FileController {

    private final UserService userService;
    private final String uploadPath;
    private final String port;

    // 构造器注入：userService 由 Spring 装配，uploadPath/port 从配置文件读取
    public FileController(
            UserService userService,
            @Value("${upload.path:uploads}") String uploadPath,
            @Value("${server.port:8080}") String port) {
        this.userService = userService;
        this.uploadPath = uploadPath;
        this.port = port;
    }

    @PostMapping("/avatar")
    public Map<String, Object> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        if (file == null || file.isEmpty()) {
            result.put("success", false);
            result.put("message", "文件不能为空");
            return result;
        }

        // 校验文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            result.put("success", false);
            result.put("message", "只支持图片格式");
            return result;
        }

        // 生成文件名（保留原扩展名）
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;

        try {
            // 必须解析成绝对路径，相对路径会按 Tomcat 临时目录解析导致 500
            Path dirPath = Paths.get(uploadPath).toAbsolutePath();
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            Path destPath = dirPath.resolve(fileName);

            // 用流拷贝，比 transferTo 更稳定（不受临时目录影响）
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, destPath, StandardCopyOption.REPLACE_EXISTING);
            }

            String url = "http://localhost:" + port + "/uploads/" + fileName;

            // 上传成功后直接把头像 URL 更新到当前用户
            Object userIdAttr = request.getAttribute("userId");
            if (userIdAttr instanceof Long) {
                userService.updateProfile((Long) userIdAttr, null, url);
            }

            result.put("success", true);
            result.put("url", url);
            result.put("message", "头像上传成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "文件保存失败：" + e.getMessage());
        }
        return result;
    }
}
