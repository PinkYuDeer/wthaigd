package com.pinkyudeer.wthaigd.task.entity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pinkyudeer.wthaigd.annotation.Table;

import lombok.Data;

@Table(name = "players")
@Data
public class Player {

    // 基础属性
    @Nonnull
    private UUID id;
    @Nonnull
    private String playerName; // 唯一用户名
    @Nullable
    private String displayName; // 显示名称

    // 积分系统
    @Nonnull
    private Integer points = 0; // 积分数
    @Nonnull
    private Integer level = 0; // 等级
    @Nonnull
    private Integer editorPoints = 0; // 编辑量积分
    @Nonnull
    private Integer workPoints = 0; // 工作积分

    // 团队关联
    @Nonnull
    private Integer teamCount = 0; // 所属团队数目

    // 通知管理
    @Nonnull
    private Integer unreadNotificationCount = 0; // 未读通知数量
    @Nonnull
    private Integer notificationCount = 0; // 被通知总数

    // 社交关系
    @Nonnull
    private Integer followingCount = 0; // 关注的用户数量
    @Nonnull
    private Integer followerCount = 0; // 粉丝用户数量
    @Nonnull
    private Integer receivedLikesCount = 0; // 个人主页获赞数
    @Nonnull
    private Integer likesCount = 0; // 点赞数
    @Nonnull
    private Integer commentsCount = 0; // 评论数

    // 玩家统计
    @Nonnull
    private Integer completedTaskCount = 0; // 完成任务数
    @Nonnull
    private Integer receivedTaskCount = 0; // 领取任务数
    @Nonnull
    private Integer assignedTaskCount = 0; // 发配任务数
    @Nonnull
    private Integer followedTaskCount = 0; // 关注任务数
    @Nonnull
    private Integer likedTaskCount = 0; // 点赞任务数
    @Nonnull
    private Integer commentedTaskCount = 0; // 评论任务数
    @Nonnull
    private Integer beFollowedTaskCount = 0; // 被关注的任务数
    @Nonnull
    private Integer beLikedTaskCount = 0; // 被点赞的任务数
    @Nonnull
    private Integer beCommentedTaskCount = 0; // 被评论的任务数
    @Nonnull
    private Integer beReceivedTaskCount = 0; // 被领取的任务数
    @Nonnull
    private Integer beAssignedTaskCount = 0; // 被分配的任务数
    @Nonnull
    private Integer beCompletedTaskCount = 0; // 被完成的任务数

    // 时间追踪
    @Nonnull
    private Duration totalTaskDuration = Duration.ZERO; // 累计任务耗时

    // 安全相关
    @Nonnull
    private PlayerRole role = PlayerRole.GUEST;
    @Nonnull
    private AccountStatus status = AccountStatus.ACTIVE;
    @Nonnull
    private LocalDateTime lastLoginTime = LocalDateTime.now();

    // 时间戳
    @Nonnull
    private LocalDateTime createTime = LocalDateTime.now();
    @Nonnull
    private LocalDateTime updateTime = LocalDateTime.now();

    // 枚举定义
    public enum PlayerRole {
        SUPER_ADMIN, // 超级管理员
        TEAM_ADMIN, // 团队管理员
        MEMBER, // 普通成员
        GUEST // 访客
    }

    public enum AccountStatus {
        ACTIVE, // 已激活
        SUSPENDED, // 已封禁
        DELETED // 已删除
    }

    /**
     * 简化后的构造函数
     */
    public Player(@Nonnull String playerName, @Nonnull UUID uuid) {
        this.playerName = playerName;
        this.id = uuid;
    }
}
