package com.onehourjob.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.onehourjob.entity.Job;
import com.onehourjob.mapper.JobMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobService {

    private final JobMapper jobMapper;

    public JobService(JobMapper jobMapper) {
        this.jobMapper = jobMapper;
    }

    public List<Job> listByKeyword(String keyword) {
        LambdaQueryWrapper<Job> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(Job::getTitle, keyword)
                    .or()
                    .like(Job::getCompanyName, keyword)
                    .or()
                    .like(Job::getCategory, keyword);
        }
        wrapper.orderByDesc(Job::getId);
        return jobMapper.selectList(wrapper);
    }

    public Job getById(Long id) {
        return jobMapper.selectById(id);
    }
}