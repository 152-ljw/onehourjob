package com.onehourjob.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.onehourjob.entity.SysUser;
import com.onehourjob.mapper.SysUserMapper;
import com.onehourjob.util.JwtUtil;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    private final SysUserMapper sysUserMapper;
    private final JwtUtil jwtUtil;

    public UserService(SysUserMapper sysUserMapper, JwtUtil jwtUtil) {
        this.sysUserMapper = sysUserMapper;
        this.jwtUtil = jwtUtil;
    }

    public Map<String, Object> register(SysUser user) {
        Map<String, Object> result = new HashMap<>();

        if (user.getUsername() == null || user.getUsername().isBlank()
                || user.getPassword() == null || user.getPassword().isBlank()) {
            result.put("success", false);
            result.put("message", "用户名和密码不能为空");
            return result;
        }

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, user.getUsername());
        SysUser exist = sysUserMapper.selectOne(wrapper);
        if (exist != null) {
            result.put("success", false);
            result.put("message", "用户名已被注册：" + user.getUsername());
            return result;
        }

        user.setId(null);
        sysUserMapper.insert(user);

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        result.put("success", true);
        result.put("message", "注册成功");
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        return result;
    }

    public Map<String, Object> login(SysUser user) {
        Map<String, Object> result = new HashMap<>();

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, user.getUsername());
        SysUser exist = sysUserMapper.selectOne(wrapper);
        if (exist == null) {
            result.put("success", false);
            result.put("message", "用户不存在，请先注册");
            return result;
        }

        if (!exist.getPassword().equals(user.getPassword())) {
            result.put("success", false);
            result.put("message", "密码错误");
            return result;
        }

        String token = jwtUtil.generateToken(exist.getId(), exist.getUsername());
        result.put("success", true);
        result.put("message", "登录成功");
        result.put("token", token);
        result.put("userId", exist.getId());
        result.put("username", exist.getUsername());
        result.put("nickname", exist.getNickname());
        result.put("avatar", exist.getAvatar());
        return result;
    }

    /**
     * 修改密码：校验旧密码，更新为新密码
     */
    public Map<String, Object> changePassword(Long userId, String oldPassword, String newPassword) {
        Map<String, Object> result = new HashMap<>();
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }
        if (oldPassword == null || oldPassword.isBlank() || newPassword == null || newPassword.isBlank()) {
            result.put("success", false);
            result.put("message", "密码不能为空");
            return result;
        }
        if (!user.getPassword().equals(oldPassword)) {
            result.put("success", false);
            result.put("message", "原密码不正确");
            return result;
        }
        if (newPassword.length() < 6 || newPassword.length() > 20) {
            result.put("success", false);
            result.put("message", "新密码需为6~20位");
            return result;
        }
        user.setPassword(newPassword);
        sysUserMapper.updateById(user);
        result.put("success", true);
        result.put("message", "密码修改成功");
        return result;
    }

    public Map<String, Object> updateProfile(Long userId, String nickname, String avatar) {
        Map<String, Object> result = new HashMap<>();
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }
        user.setNickname(nickname);
        user.setAvatar(avatar);
        sysUserMapper.updateById(user);
        result.put("success", true);
        result.put("message", "个人信息更新成功");
        return result;
    }
}