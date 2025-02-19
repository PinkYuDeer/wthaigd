package com.pinkyudeer.wthaigd.task.entity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

    // getter和setter方法
    @Nonnull
    public UUID getId() {
        return id;
    }

    public void setId(@Nonnull UUID id) {
        this.id = id;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    public void setName(@Nonnull String name) {
        this.name = name;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    @Nonnull
    public String getTeamCode() {
        return teamCode;
    }

    public void setTeamCode(@Nonnull String teamCode) {
        this.teamCode = teamCode;
    }

    @Nonnull
    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(@Nonnull UUID ownerId) {
        this.ownerId = ownerId;
    }

    @Nonnull
    public Integer getTotalMembers() {
        return totalMembers;
    }

    public void setTotalMembers(@Nonnull Integer totalMembers) {
        this.totalMembers = totalMembers;
    }

    @Nonnull
    public Integer getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(@Nonnull Integer totalTasks) {
        this.totalTasks = totalTasks;
    }

    @Nonnull
    public Integer getTotalTags() {
        return totalTags;
    }

    public void setTotalTags(@Nonnull Integer totalTags) {
        this.totalTags = totalTags;
    }

    @Nonnull
    public Task.PrivacyLevel getDefaultTaskVisibility() {
        return defaultTaskVisibility;
    }

    public void setDefaultTaskVisibility(@Nonnull Task.PrivacyLevel defaultTaskVisibility) {
        this.defaultTaskVisibility = defaultTaskVisibility;
    }

    @Nonnull
    public Integer getTaskDone() {
        return taskDone;
    }

    public void setTaskDone(@Nonnull Integer taskDone) {
        this.taskDone = taskDone;
    }

    @Nonnull
    public Integer getTaskGet() {
        return taskGet;
    }

    public void setTaskGet(@Nonnull Integer taskGet) {
        this.taskGet = taskGet;
    }

    @Nonnull
    public Integer getTaskFollowed() {
        return taskFollowed;
    }

    public void setTaskFollowed(@Nonnull Integer taskFollowed) {
        this.taskFollowed = taskFollowed;
    }

    @Nonnull
    public Integer getTaskLiked() {
        return taskLiked;
    }

    public void setTaskLiked(@Nonnull Integer taskLiked) {
        this.taskLiked = taskLiked;
    }

    @Nonnull
    public Integer getTaskCommented() {
        return taskCommented;
    }

    public void setTaskCommented(@Nonnull Integer taskCommented) {
        this.taskCommented = taskCommented;
    }

    @Nonnull
    public Integer getTaskBeDone() {
        return taskBeDone;
    }

    public void setTaskBeDone(@Nonnull Integer taskBeDone) {
        this.taskBeDone = taskBeDone;
    }

    @Nonnull
    public Integer getTaskBeGet() {
        return taskBeGet;
    }

    public void setTaskBeGet(@Nonnull Integer taskBeGet) {
        this.taskBeGet = taskBeGet;
    }

    @Nonnull
    public Integer getTaskBeFollowed() {
        return taskBeFollowed;
    }

    public void setTaskBeFollowed(@Nonnull Integer taskBeFollowed) {
        this.taskBeFollowed = taskBeFollowed;
    }

    @Nonnull
    public Integer getTaskBeLiked() {
        return taskBeLiked;
    }

    public void setTaskBeLiked(@Nonnull Integer taskBeLiked) {
        this.taskBeLiked = taskBeLiked;
    }

    @Nonnull
    public Integer getTaskBeCommented() {
        return taskBeCommented;
    }

    public void setTaskBeCommented(@Nonnull Integer taskBeCommented) {
        this.taskBeCommented = taskBeCommented;
    }

    @Nonnull
    public Long getRewardPoints() {
        return rewardPoints;
    }

    public void setRewardPoints(@Nonnull Long rewardPoints) {
        this.rewardPoints = rewardPoints;
    }

    @Nonnull
    public Long getEditPoints() {
        return editPoints;
    }

    public void setEditPoints(@Nonnull Long editPoints) {
        this.editPoints = editPoints;
    }

    @Nonnull
    public Long getWorkPoints() {
        return workPoints;
    }

    public void setWorkPoints(@Nonnull Long workPoints) {
        this.workPoints = workPoints;
    }

    @Nonnull
    public Integer getLevel() {
        return level;
    }

    public void setLevel(@Nonnull Integer level) {
        this.level = level;
    }

    @Nonnull
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(@Nonnull LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    @Nullable
    public LocalDateTime getDisbandTime() {
        return disbandTime;
    }

    public void setDisbandTime(@Nullable LocalDateTime disbandTime) {
        this.disbandTime = disbandTime;
    }

    @Nonnull
    public PermissionSettings getPermissions() {
        return permissions;
    }

    public void setPermissions(@Nonnull PermissionSettings permissions) {
        this.permissions = permissions;
    }

    @Nonnull
    public Integer getJoinRequestsCount() {
        return joinRequestsCount;
    }

    public void setJoinRequestsCount(@Nonnull Integer joinRequestsCount) {
        this.joinRequestsCount = joinRequestsCount;
    }

    @Nonnull
    public Integer getInvitationsCount() {
        return invitationsCount;
    }

    public void setInvitationsCount(@Nonnull Integer invitationsCount) {
        this.invitationsCount = invitationsCount;
    }
}
