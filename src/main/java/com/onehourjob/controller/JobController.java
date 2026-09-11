package com.onehourjob.controller;

import com.onehourjob.entity.Job;
import com.onehourjob.service.JobService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping
    public Map<String, Object> list(@RequestParam(required = false) String keyword) {
        List<Job> jobs = jobService.listByKeyword(keyword);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("total", jobs.size());
        result.put("jobs", jobs);
        return result;
    }

    @GetMapping("/{id}")
    public Map<String, Object> detail(@PathVariable Long id) {
        Job job = jobService.getById(id);
        Map<String, Object> result = new HashMap<>();
        if (job == null) {
            result.put("success", false);
            result.put("message", "岗位不存在，id=" + id);
        } else {
            result.put("success", true);
            result.put("job", job);
        }
        return result;
    }
}