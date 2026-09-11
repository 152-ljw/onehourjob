package com.onehourjob.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser {

    /** 主键ID，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名 */
    private String username;

    /** 密码，明文存储 */
    private String password;

    /** 手机号 */
    private String phone;

    /** 用户昵称 */
    private String nickname;

    /** 头像URL */
    private String avatar;

    /** 注册时间，数据库自动生成 */
    private LocalDateTime createdAt;
}