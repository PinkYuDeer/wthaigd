package com.pinkyudeer.wthaigd.task.entity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pinkyudeer.wthaigd.annotation.Column;
import com.pinkyudeer.wthaigd.annotation.FieldCheck;
import com.pinkyudeer.wthaigd.annotation.Table;

import lombok.Data;

@Data
@Table(name = "players")
public class Player {

    // 基础属性
    @Nonnull
    @FieldCheck(type = FieldCheck.Type.UUID, dataType = UUID.class)
    @Column(name = "id", isPrimaryKey = true)
    private UUID id;
    @Nonnull
    @FieldCheck(type = FieldCheck.Type.LENGTH, dataType = String.class, min = "1", max = "256")
    @Column(name = "player_name", isUnique = true, index = { "idx_players_player_name" })
    private String playerName; // 唯一用户名
    @Nullable
    @FieldCheck(type = FieldCheck.Type.LENGTH, dataType = String.class, min = "1", max = "256")
    @Column(name = "display_name", isUnique = true, index = { "idx_players_display_name" })
    private String displayName; // 显示名称

    // 积分系统
    @Nonnull
    @FieldCheck(type = FieldCheck.Type.MIN, dataType = Integer.class, min = "0")
    @Column(name = "points", defaultValue = "0")
    private Integer points = 0; // 积分数
    @Nonnull
    @FieldCheck(type = FieldCheck.Type.MIN, dataType = Integer.class, min = "0")
    @Column(name = "level", defaultValue = "0")
    private Integer level = 0; // 等级
    @Nonnull
    @FieldCheck(type = FieldCheck.Type.MIN, dataType = Integer.class, min = "0")
    @Column(name = "editor_points", defaultValue = "0")
    private Integer editorPoints = 0; // 编辑量积分
    @Nonnull
    @FieldCheck(type = FieldCheck.Type.MIN, dataType = Integer.class, min = "0")
    @Column(name = "work_points", defaultValue = "0")
    private Integer workPoints = 0; // 工作积分

    // 团队关联
    @Nonnull
    @FieldCheck(type = FieldCheck.Type.MIN, dataType = Integer.class, min = "0")
    @Column(name = "team_count", defaultValue = "0")
    private Integer teamCount = 0; // 所属团队数目

    // 通知管理
    @Nonnull
    @FieldCheck(type = FieldCheck.Type.MIN, dataType = Integer.class, min = "0")
    @Column(name = "unread_notification_count", defaultValue = "0")
    private Integer unreadNotificationCount = 0; // 未读通知数量
    @Nonnull
    @FieldCheck(type = FieldCheck.Type.MIN, dataType = Integer.class, min = "0")
    @Column(name = "notification_count", defaultValue = "0")
    private Integer notificationCount = 0; // 被通知总数

    // 社交关系
    @Nonnull
    @FieldCheck(type = FieldCheck.Type.MIN, dataType = Integer.class, min = "0")
    @Column(name = "following_count", defaultValue = "0")
    private Integer followingCount = 0; // 关注的用户数量
    @Nonnull
    @FieldCheck(type = FieldCheck.Type.MIN, dataType = Integer.class, min = "0")
    @Column(name = "follower_count", defaultValue = "0")
    private Integer followerCount = 0; // 粉丝用户数量
    @Nonnull
    @FieldCheck(type = FieldCheck.Type.MIN, dataType = Integer.class, min = "0")
    @Column(name = "received_likes_count", defaultValue = "0")
    private Integer receivedLikesCount = 0; // 个人主页获赞数
    @Nonnull
    @FieldCheck(type = FieldCheck.Type.MIN, dataType = Integer.class, min = "0")
    @Column(name = "likes_count", defaultValue = "0")
    private Integer likesCount = 0; // 点赞数
    @Nonnull
    @FieldCheck(type = FieldCheck.Type.MIN, dataType = Integer.class, min = "0")
    @Column(name = "comments_count", defaultValue = "0")
    private Integer commentsCount = 0; // 评论数

    // 玩家统计
    @Nonnull
    @FieldCheck(type = FieldCheck.Type.MIN, dataType = Integer.class, min = "0")
    @Column(name = "completed_task_count", defaultValue = "0")
    private Integer completedTaskCount = 0; // 完成任务数
    @Nonnull
    @FieldCheck(type = FieldCheck.Type.MIN, dataType = Integer.class, min = "0")
    @Column(name = "received_task_count", defaultValue = "0")
    private Integer receivedTaskCount = 0; // 领取任务数
    @Nonnull
    @FieldCheck(type = FieldCheck.Type.MIN, dataType = Integer.class, min = "0")
    @Column(name = "assigned_task_count", defaultValue = "0")
    private Integer assignedTaskCount = 0; // 发配任务数
    @Nonnull
    @FieldCheck(type = FieldCheck.Type.MIN, dataType = Integer.class, min = "0")
    @Column(name = "followed_task_count", defaultValue = "0")
    private Integer followedTaskCount = 0; // 关注任务数
    @Nonnull
    @FieldCheck(type = FieldCheck.Type.MIN, dataType = Integer.class, min = "0")
    @Column(name = "liked_task_count", defaultValue = "0")
    private Integer likedTaskCount = 0; // 点赞任务数
    @Nonnull
    @FieldCheck(type = FieldCheck.Type.MIN, dataType = Integer.class, min = "0")
    @Column(name = "commented_task_count", defaultValue = "0")
    private Integer commentedTaskCount = 0; // 评论任务数
    @Nonnull
    @FieldCheck(type = FieldCheck.Type.MIN, dataType = Integer.class, min = "0")
    @Column(name = "be_followed_task_count", defaultValue = "0")
    private Integer beFollowedTaskCount = 0; // 被关注的任务数
    @Nonnull
    @FieldCheck(type = FieldCheck.Type.MIN, dataType = Integer.class, min = "0")
    @Column(name = "be_liked_task_count", defaultValue = "0")
    private Integer beLikedTaskCount = 0; // 被点赞的任务数
    @Nonnull
    @FieldCheck(type = FieldCheck.Type.MIN, dataType = Integer.class, min = "0")
    @Column(name = "be_commented_task_count", defaultValue = "0")
    private Integer beCommentedTaskCount = 0; // 被评论的任务数
    @Nonnull
    @FieldCheck(type = FieldCheck.Type.MIN, dataType = Integer.class, min = "0")
    @Column(name = "be_received_task_count", defaultValue = "0")
    private Integer beReceivedTaskCount = 0; // 被领取的任务数
    @Nonnull
    @FieldCheck(type = FieldCheck.Type.MIN, dataType = Integer.class, min = "0")
    @Column(name = "be_assigned_task_count", defaultValue = "0")
    private Integer beAssignedTaskCount = 0; // 被分配的任务数
    @Nonnull
    @FieldCheck(type = FieldCheck.Type.MIN, dataType = Integer.class, min = "0")
    @Column(name = "be_completed_task_count", defaultValue = "0")
    private Integer beCompletedTaskCount = 0; // 被完成的任务数

    // 时间追踪
    @Nonnull
    @FieldCheck(type = FieldCheck.Type.MIN, dataType = Duration.class, min = "0")
    @Column(name = "total_task_duration", defaultValue = "0")
    private Duration totalTaskDuration = Duration.ZERO; // 累计任务耗时

    // 安全相关
    @Nonnull
    @FieldCheck(type = FieldCheck.Type.ENUM, dataType = PlayerRole.class)
    @Column(name = "role", defaultValue = "'GUEST'")
    private PlayerRole role = PlayerRole.GUEST;
    @Nonnull
    @FieldCheck(type = FieldCheck.Type.ENUM, dataType = AccountStatus.class)
    @Column(name = "status", defaultValue = "'ACTIVE'")
    private AccountStatus status = AccountStatus.ACTIVE;
    @Nonnull
    @Column(name = "last_login_time", defaultValue = "CURRENT_TIMESTAMP")
    private LocalDateTime lastLoginTime = LocalDateTime.now();

    // 时间戳
    @Nonnull
    @Column(name = "create_time", defaultValue = "CURRENT_TIMESTAMP")
    private LocalDateTime createTime = LocalDateTime.now();
    @Nonnull
    @Column(name = "update_time", defaultValue = "CURRENT_TIMESTAMP")
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
