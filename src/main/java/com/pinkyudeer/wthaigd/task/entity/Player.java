package com.pinkyudeer.wthaigd.task.entity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

    // getter和setter方法
    @Nonnull
    public UUID getId() {
        return id;
    }

    public void setId(@Nonnull UUID id) {
        this.id = id;
    }

    @Nonnull
    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(@Nonnull String playerName) {
        this.playerName = playerName;
    }

    @Nullable
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(@Nullable String displayName) {
        this.displayName = displayName;
    }

    @Nonnull
    public Integer getPoints() {
        return points;
    }

    public void setPoints(@Nonnull Integer points) {
        this.points = points;
    }

    @Nonnull
    public Integer getLevel() {
        return level;
    }

    public void setLevel(@Nonnull Integer level) {
        this.level = level;
    }

    @Nonnull
    public Integer getEditorPoints() {
        return editorPoints;
    }

    public void setEditorPoints(@Nonnull Integer editorPoints) {
        this.editorPoints = editorPoints;
    }

    @Nonnull
    public Integer getWorkPoints() {
        return workPoints;
    }

    public void setWorkPoints(@Nonnull Integer workPoints) {
        this.workPoints = workPoints;
    }

    @Nonnull
    public Integer getTeamCount() {
        return teamCount;
    }

    public void setTeamCount(@Nonnull Integer teamCount) {
        this.teamCount = teamCount;
    }

    @Nonnull
    public Integer getUnreadNotificationCount() {
        return unreadNotificationCount;
    }

    public void setUnreadNotificationCount(@Nonnull Integer unreadNotificationCount) {
        this.unreadNotificationCount = unreadNotificationCount;
    }

    @Nonnull
    public Integer getNotificationCount() {
        return notificationCount;
    }

    public void setNotificationCount(@Nonnull Integer notificationCount) {
        this.notificationCount = notificationCount;
    }

    @Nonnull
    public Integer getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(@Nonnull Integer followingCount) {
        this.followingCount = followingCount;
    }

    @Nonnull
    public Integer getFollowerCount() {
        return followerCount;
    }

    public void setFollowerCount(@Nonnull Integer followerCount) {
        this.followerCount = followerCount;
    }

    @Nonnull
    public Integer getReceivedLikesCount() {
        return receivedLikesCount;
    }

    public void setReceivedLikesCount(@Nonnull Integer receivedLikesCount) {
        this.receivedLikesCount = receivedLikesCount;
    }

    @Nonnull
    public Integer getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(@Nonnull Integer likesCount) {
        this.likesCount = likesCount;
    }

    @Nonnull
    public Integer getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(@Nonnull Integer commentsCount) {
        this.commentsCount = commentsCount;
    }

    @Nonnull
    public Integer getCompletedTaskCount() {
        return completedTaskCount;
    }

    public void setCompletedTaskCount(@Nonnull Integer completedTaskCount) {
        this.completedTaskCount = completedTaskCount;
    }

    @Nonnull
    public Integer getReceivedTaskCount() {
        return receivedTaskCount;
    }

    public void setReceivedTaskCount(@Nonnull Integer receivedTaskCount) {
        this.receivedTaskCount = receivedTaskCount;
    }

    @Nonnull
    public Integer getAssignedTaskCount() {
        return assignedTaskCount;
    }

    public void setAssignedTaskCount(@Nonnull Integer assignedTaskCount) {
        this.assignedTaskCount = assignedTaskCount;
    }

    @Nonnull
    public Integer getFollowedTaskCount() {
        return followedTaskCount;
    }

    public void setFollowedTaskCount(@Nonnull Integer followedTaskCount) {
        this.followedTaskCount = followedTaskCount;
    }

    @Nonnull
    public Integer getLikedTaskCount() {
        return likedTaskCount;
    }

    public void setLikedTaskCount(@Nonnull Integer likedTaskCount) {
        this.likedTaskCount = likedTaskCount;
    }

    @Nonnull
    public Integer getCommentedTaskCount() {
        return commentedTaskCount;
    }

    public void setCommentedTaskCount(@Nonnull Integer commentedTaskCount) {
        this.commentedTaskCount = commentedTaskCount;
    }

    @Nonnull
    public Integer getBeFollowedTaskCount() {
        return beFollowedTaskCount;
    }

    public void setBeFollowedTaskCount(@Nonnull Integer beFollowedTaskCount) {
        this.beFollowedTaskCount = beFollowedTaskCount;
    }

    @Nonnull
    public Integer getBeLikedTaskCount() {
        return beLikedTaskCount;
    }

    public void setBeLikedTaskCount(@Nonnull Integer beLikedTaskCount) {
        this.beLikedTaskCount = beLikedTaskCount;
    }

    @Nonnull
    public Integer getBeCommentedTaskCount() {
        return beCommentedTaskCount;
    }

    public void setBeCommentedTaskCount(@Nonnull Integer beCommentedTaskCount) {
        this.beCommentedTaskCount = beCommentedTaskCount;
    }

    @Nonnull
    public Integer getBeReceivedTaskCount() {
        return beReceivedTaskCount;
    }

    public void setBeReceivedTaskCount(@Nonnull Integer beReceivedTaskCount) {
        this.beReceivedTaskCount = beReceivedTaskCount;
    }

    @Nonnull
    public Integer getBeAssignedTaskCount() {
        return beAssignedTaskCount;
    }

    public void setBeAssignedTaskCount(@Nonnull Integer beAssignedTaskCount) {
        this.beAssignedTaskCount = beAssignedTaskCount;
    }

    @Nonnull
    public Integer getBeCompletedTaskCount() {
        return beCompletedTaskCount;
    }

    public void setBeCompletedTaskCount(@Nonnull Integer beCompletedTaskCount) {
        this.beCompletedTaskCount = beCompletedTaskCount;
    }

    @Nonnull
    public Duration getTotalTaskDuration() {
        return totalTaskDuration;
    }

    public void setTotalTaskDuration(@Nonnull Duration totalTaskDuration) {
        this.totalTaskDuration = totalTaskDuration;
    }

    @Nonnull
    public PlayerRole getRole() {
        return role;
    }

    public void setRole(@Nonnull PlayerRole role) {
        this.role = role;
    }

    @Nonnull
    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(@Nonnull AccountStatus status) {
        this.status = status;
    }

    @Nonnull
    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(@Nonnull LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    @Nonnull
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(@Nonnull LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Nonnull
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(@Nonnull LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
