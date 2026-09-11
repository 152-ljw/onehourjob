package com.onehourjob.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.onehourjob.entity.Application;
import com.onehourjob.entity.Job;
import com.onehourjob.mapper.ApplicationMapper;
import com.onehourjob.mapper.JobMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ApplicationService {

    private final ApplicationMapper applicationMapper;
    private final JobMapper jobMapper;

    public ApplicationService(ApplicationMapper applicationMapper, JobMapper jobMapper) {
        this.applicationMapper = applicationMapper;
        this.jobMapper = jobMapper;
    }

    public Map<String, Object> apply(long userId, long jobId) {
        Map<String, Object> result = new HashMap<>();

        if (userId == 0) {
            result.put("success", false);
            result.put("message", "用户ID不能为空");
            return result;
        }
        if (jobId == 0) {
            result.put("success", false);
            result.put("message", "岗位ID不能为空");
            return result;
        }

        Job job = jobMapper.selectById(jobId);
        if (job == null) {
            result.put("success", false);
            result.put("message", "岗位不存在，id=" + jobId);
            return result;
        }

        LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Application::getUserId, userId)
                .eq(Application::getJobId, jobId);
        Application exist = applicationMapper.selectOne(wrapper);
        if (exist != null) {
            result.put("success", false);
            result.put("message", "你已经投递过这个岗位，不能重复投递");
            return result;
        }

        Application application = new Application();
        application.setUserId(userId);
        application.setJobId(jobId);
        applicationMapper.insert(application);

        result.put("success", true);
        result.put("message", "投递成功");
        return result;
    }

    public List<Map<String, Object>> listByUserId(Long userId) {
        LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Application::getUserId, userId)
                .orderByDesc(Application::getAppliedAt);
        List<Application> applications = applicationMapper.selectList(wrapper);

        List<Long> jobIds = applications.stream()
                .map(Application::getJobId)
                .collect(Collectors.toList());
        List<Job> jobs = jobMapper.selectBatchIds(jobIds);
        Map<Long, Job> jobMap = jobs.stream()
                .collect(Collectors.toMap(Job::getId, j -> j));

        List<Map<String, Object>> list = new ArrayList<>();
        for (Application app : applications) {
            Job job = jobMap.get(app.getJobId());
            Map<String, Object> item = new HashMap<>();
            item.put("jobId", app.getJobId());
            item.put("title", job != null ? job.getTitle() : "");
            item.put("companyName", job != null ? job.getCompanyName() : "");
            item.put("salary", job != null ? job.getSalary() : "");
            item.put("category", job != null ? job.getCategory() : "");
            item.put("status", app.getStatus());
            item.put("appliedAt", app.getAppliedAt());
            list.add(item);
        }
        return list;
    }
}