package com.pinkyudeer.wthaigd.task.entity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pinkyudeer.wthaigd.annotation.Table;

import lombok.Data;

@Data
@Table(name = "teams")
public class Team {

    // 基础信息
    @Nonnull
    private UUID id = UUID.randomUUID(); // 团队ID
    @Nonnull
    private String name; // 团队名称
    @Nullable
    private String description; // 团队描述
    @Nonnull
    private String teamCode; // 团队加入码

    // 统计信息
    @Nonnull
    private UUID ownerId; // 拥有者ID
    @Nonnull
    private Integer totalMembers = 0; // 总成员数
    @Nonnull
    private Integer totalTasks = 0; // 总任务数
    @Nonnull
    private Integer totalTags = 0; // 总标签数
    @Nonnull
    private Task.PrivacyLevel defaultTaskVisibility = Task.PrivacyLevel.TEAM; // 默认任务可见性

    // 主动操作任务统计
    @Nonnull
    private Integer taskDone = 0; // 完成任务数
    @Nonnull
    private Integer taskGet = 0; // 领取任务数
    @Nonnull
    private Integer taskFollowed = 0; // 关注任务数
    @Nonnull
    private Integer taskLiked = 0; // 点赞任务数
    @Nonnull
    private Integer taskCommented = 0; // 评论任务数

    // 被操作任务统计
    @Nonnull
    private Integer taskBeDone = 0; // 任务被完成数
    @Nonnull
    private Integer taskBeGet = 0; // 任务被领取数
    @Nonnull
    private Integer taskBeFollowed = 0; // 任务被关注数
    @Nonnull
    private Integer taskBeLiked = 0; // 任务被点赞数
    @Nonnull
    private Integer taskBeCommented = 0; // 任务被评论数

    // 奖励机制
    @Nonnull
    private Long rewardPoints = 0L; // 团队积分
    @Nonnull
    private Long editPoints = 0L; // 编辑积分
    @Nonnull
    private Long workPoints = 0L; // 工作积分
    @Nonnull
    private Integer level = 0; // 团队等级

    // 时间信息
    @Nonnull
    private LocalDateTime updateTime = LocalDateTime.now(); // 更新时间
    @Nullable
    private LocalDateTime disbandTime; // 解散时间

    // 权限设置
    @Nonnull
    private PermissionSettings permissions = new PermissionSettings(); // 权限设置

    // 请求信息
    @Nonnull
    private Integer joinRequestsCount = 0; // 加入请求列表
    @Nonnull
    private Integer invitationsCount = 0; // 邀请列表

    public enum TeamRole {
        ADMIN,
        MEMBER,
        GUEST
    }

    public static class PermissionSettings {

        public boolean allowMemberCreateTask;
        public boolean allowMemberAssignTask;
        public Task.Priority minAssignPriority;
        public Duration maxTaskDuration;
    }

    public Team(@Nonnull String name, @Nonnull UUID ownerId, @Nullable String description) {
        this.name = name;
        this.ownerId = ownerId;
        if ("".equals(description)) {
            this.description = null;
        } else {
            this.description = description;
        }
        this.teamCode = UUID.randomUUID()
            .toString()
            .substring(0, 8) + "_"
            + Long.toHexString(System.currentTimeMillis());
    }
}
