package com.onehourjob.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.onehourjob.entity.Resume;
import com.onehourjob.mapper.ResumeMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ResumeService {

    private final ResumeMapper resumeMapper;

    public ResumeService(ResumeMapper resumeMapper) {
        this.resumeMapper = resumeMapper;
    }

    public Resume getByUserId(Long userId) {
        LambdaQueryWrapper<Resume> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Resume::getUserId, userId);
        return resumeMapper.selectOne(wrapper);
    }

    public Map<String, Object> save(Resume resume) {
        Map<String, Object> result = new HashMap<>();

        LambdaQueryWrapper<Resume> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Resume::getUserId, resume.getUserId());
        Resume exist = resumeMapper.selectOne(wrapper);
        if (exist != null) {
            resume.setId(exist.getId());
            resumeMapper.updateById(resume);
        } else {
            resumeMapper.insert(resume);
        }

        result.put("success", true);
        result.put("message", "简历保存成功");
        return result;
    }
}