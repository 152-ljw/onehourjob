package com.onehourjob.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("job")
public class Job {

    /** 主键ID，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 岗位名称 */
    private String title;

    /** 公司名称 */
    private String companyName;

    /** 岗位描述 */
    private String description;

    /** 任职要求 */
    private String requirements;

    /** 岗位分类 */
    private String category;

    /** 薪资范围 */
    private String salary;
}