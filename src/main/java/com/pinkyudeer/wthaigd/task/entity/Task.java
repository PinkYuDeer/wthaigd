package com.pinkyudeer.wthaigd.task.entity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Task {

    // task 基础属性
    @Nonnull
    private String id = UUID.randomUUID()
        .toString(); // 任务的唯一标识符，用 UUID
    @Nonnull
    private String title; // 任务标题
    @Nonnull
    private String description; // 任务描述
    @Nonnull
    private Integer version = 0; // 修订次数
    @Nonnull
    private PrivacyLevel visibility = PrivacyLevel.PRIVATE; // 隐私级别（枚举类型）
    @Nullable
    private TaskType taskType; // 任务类型

    // task 参与者属性
    @Nonnull
    private UUID creator; // 创建者
    @Nullable
    private Integer assigneeCount; // 负责人个数
    @Nullable
    private UUID teamId; // 所属团队ID
    @Nonnull
    private Integer followerCount = 0; // 关注者数量
    @Nonnull
    private Integer likeCount = 0; // 点赞数量
    @Nonnull
    private Integer commentCount = 0; // 评论数量
    @Nullable
    private UUID lastOperator; // 最后操作人

    // task 状态属性
    @Nonnull
    private Priority priority; // 优先级（枚举类型）
    @Nonnull
    private Importance importance = Importance.UNDEFINED; // 重要程度（枚举类型）
    @Nonnull
    private Urgency urgency = Urgency.UNDEFINED; // 紧急程度（枚举类型）
    @Nonnull
    private TaskStatus status = TaskStatus.UnClaimed; // 状态（枚举类型）

    // task 时间属性
    @Nonnull
    private LocalDateTime createTime = LocalDateTime.now(); // 创建时间
    @Nullable
    private LocalDateTime startTime; // 开始时间.
    @Nullable
    private LocalDateTime endTime; // 完成时间
    @Nonnull
    private LocalDateTime updateTime = LocalDateTime.now(); // 更新时间
    @Nullable
    private LocalDateTime deadline; // 截止时间
    @Nullable
    private Duration estimatedDuration; // 预估耗时
    @Nullable
    private Duration actualDuration; // 实际耗时
    @Nullable
    private LocalDateTime reminderTime; // 提醒时间
    @Nullable
    private String recurrenceRule; // 国际标准的RRule,例如"FREQ=WEEKLY;INTERVAL=2;COUNT=5"
    @Nullable
    private LocalDateTime repeatTime; // 重复时间
    @Nullable
    private Duration repeatInterval; // 重复时间间隔
    @Nullable
    private LocalDateTime repeatEndTime; // 重复结束时间
    @Nullable
    private Integer repeatCount; // 重复次数

    // 构造函数
    public Task(@Nonnull String title, @Nonnull String description, @Nonnull UUID creator) {
        this.title = title;
        this.description = description;
        this.creator = creator;
        this.priority = calculatePriority(this.importance, this.urgency);
    }

    // 带有重要程度和紧急程度的构造函数
    public Task(String title, String description, @Nonnull UUID creator, @Nonnull Importance importance,
        @Nonnull Urgency urgency) {
        this(title, description, creator);
        this.importance = importance;
        this.urgency = urgency;
        this.priority = calculatePriority(importance, urgency);
    }

    // 内部枚举类
    // TODO: 允许用户自定义优先级
    // 定义重要程度
    public enum Importance {
        UNDEFINED, // 未定义
        LOW, // 低重要性
        MEDIUM, // 中重要性
        HIGH, // 高重要性
        CRITICAL // 特殊最高重要性
    }

    // 定义紧急程度
    public enum Urgency {
        UNDEFINED, // 未定义
        LOW, // 低紧急性
        MEDIUM, // 中紧急性
        HIGH, // 高紧急性
        CRITICAL // 特殊最高紧急性
    }

    // 定义优先级
    public enum Priority {
        CRITICAL, // 最特殊优先级
        P1, // 优先级 1
        P2, // 优先级 2
        P3, // 优先级 3
        P4, // 优先级 4
        P5, // 优先级 5
        P6, // 优先级 6
        P7, // 优先级 7
        P8, // 优先级 8
        P9, // 优先级 9
        UNDEFINED, // 未定义
    }

    private static Priority calculatePriority(Importance importance, Urgency urgency) {
        return switch (importance) {
            case CRITICAL -> switch (urgency) {
                    case CRITICAL -> Priority.CRITICAL;
                    case HIGH -> Priority.P1;
                    case MEDIUM, UNDEFINED -> Priority.P2;
                    case LOW -> Priority.P3;
                };
            case HIGH -> switch (urgency) {
                    case CRITICAL -> Priority.P2;
                    case HIGH -> Priority.P3;
                    case MEDIUM, UNDEFINED -> Priority.P4;
                    case LOW -> Priority.P5;
                };
            case MEDIUM -> switch (urgency) {
                    case CRITICAL -> Priority.P3;
                    case HIGH -> Priority.P5;
                    case MEDIUM, UNDEFINED -> Priority.P6;
                    case LOW -> Priority.P7;
                };
            case LOW -> switch (urgency) {
                    case CRITICAL -> Priority.P5;
                    case HIGH -> Priority.P7;
                    case MEDIUM, UNDEFINED -> Priority.P8;
                    case LOW -> Priority.P9;
                };
            case UNDEFINED -> switch (urgency) {
                    case CRITICAL -> Priority.P2;
                    case HIGH -> Priority.P4;
                    case MEDIUM -> Priority.P6;
                    case LOW -> Priority.P8;
                    case UNDEFINED -> Priority.UNDEFINED;
                };
        };
    }

    // 定义隐私级别枚举
    public enum PrivacyLevel {
        PUBLIC, // 公开可见
        TEAM, // 团队可见
        PRIVATE // 仅自己可见
    }

    // 状态
    public enum TaskStatus {
        UnClaimed, // 待认领
        Blocked, // 被阻塞
        UnStarted, // 待开始
        InProgress, // 进行中
        InTrialRun, // 试运行
        Completed, // 已完成
        Canceled, // 已取消
        Closed, // 已关闭
        Rejected, // 已拒绝
        Postponed, // 已延期
        Defect // 有缺陷
        // TODO: 允许用户自定义状态
    }

    // 任务类型
    public enum TaskType {
        UNDEFINED, // 未定义
        MAINLINE, // 主线任务
        BRANCH, // 支线任务
        DAILY, // 日常任务
        MAGIC, // 魔法任务
        EVENT, // 事件任务
        ACHIEVEMENT, // 成就任务
        CHALLENGE, // 挑战任务
        QUEST_FINISH // 任务书补全
        // TODO: 允许用户自定义任务类型
    }

    // getter和setter方法
    @Nonnull
    public String getId() {
        return id;
    }

    public void setId(@Nonnull String id) {
        this.id = id;
    }

    @Nonnull
    public String getTitle() {
        return title;
    }

    public void setTitle(@Nonnull String title) {
        this.title = title;
    }

    @Nonnull
    public String getDescription() {
        return description;
    }

    public void setDescription(@Nonnull String description) {
        this.description = description;
    }

    @Nonnull
    public Integer getVersion() {
        return version;
    }

    public void setVersion(@Nonnull Integer version) {
        this.version = version;
    }

    @Nonnull
    public PrivacyLevel getVisibility() {
        return visibility;
    }

    public void setVisibility(@Nonnull PrivacyLevel visibility) {
        this.visibility = visibility;
    }

    @Nullable
    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(@Nullable TaskType taskType) {
        this.taskType = taskType;
    }

    @Nonnull
    public UUID getCreator() {
        return creator;
    }

    public void setCreator(@Nonnull UUID creator) {
        this.creator = creator;
    }

    @Nullable
    public Integer getAssigneeCount() {
        return assigneeCount;
    }

    public void setAssigneeCount(@Nullable Integer assigneeCount) {
        this.assigneeCount = assigneeCount;
    }

    @Nullable
    public UUID getTeamId() {
        return teamId;
    }

    public void setTeamId(@Nullable UUID teamId) {
        this.teamId = teamId;
    }

    @Nonnull
    public Integer getFollowerCount() {
        return followerCount;
    }

    public void setFollowerCount(@Nonnull Integer followerCount) {
        this.followerCount = followerCount;
    }

    @Nonnull
    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(@Nonnull Integer likeCount) {
        this.likeCount = likeCount;
    }

    @Nonnull
    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(@Nonnull Integer commentCount) {
        this.commentCount = commentCount;
    }

    @Nullable
    public UUID getLastOperator() {
        return lastOperator;
    }

    public void setLastOperator(@Nullable UUID lastOperator) {
        this.lastOperator = lastOperator;
    }

    @Nonnull
    public Priority getPriority() {
        return this.priority;
    }

    public void setPriority(@Nonnull Priority priority) {
        this.priority = priority;
    }

    @Nonnull
    public Importance getImportance() {
        return importance;
    }

    public void setImportance(@Nonnull Importance importance) {
        this.importance = importance;
    }

    @Nonnull
    public Urgency getUrgency() {
        return urgency;
    }

    public void setUrgency(@Nonnull Urgency urgency) {
        this.urgency = urgency;
    }

    @Nonnull
    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(@Nonnull TaskStatus status) {
        this.status = status;
    }

    @Nonnull
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(@Nonnull LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Nullable
    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(@Nullable LocalDateTime startTime) {
        this.startTime = startTime;
    }

    @Nullable
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(@Nullable LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Nonnull
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(@Nonnull LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    @Nullable
    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(@Nullable LocalDateTime deadline) {
        this.deadline = deadline;
    }

    @Nullable
    public Duration getEstimatedDuration() {
        return estimatedDuration;
    }

    public void setEstimatedDuration(@Nullable Duration estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    @Nullable
    public Duration getActualDuration() {
        return actualDuration;
    }

    public void setActualDuration(@Nullable Duration actualDuration) {
        this.actualDuration = actualDuration;
    }

    @Nullable
    public LocalDateTime getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(@Nullable LocalDateTime reminderTime) {
        this.reminderTime = reminderTime;
    }

    @Nullable
    public String getRecurrenceRule() {
        return recurrenceRule;
    }

    public void setRecurrenceRule(@Nullable String recurrenceRule) {
        this.recurrenceRule = recurrenceRule;
    }

    @Nullable
    public LocalDateTime getRepeatTime() {
        return repeatTime;
    }

    public void setRepeatTime(@Nullable LocalDateTime repeatTime) {
        this.repeatTime = repeatTime;
    }

    @Nullable
    public Duration getRepeatInterval() {
        return repeatInterval;
    }

    public void setRepeatInterval(@Nullable Duration repeatInterval) {
        this.repeatInterval = repeatInterval;
    }

    @Nullable
    public LocalDateTime getRepeatEndTime() {
        return repeatEndTime;
    }

    public void setRepeatEndTime(@Nullable LocalDateTime repeatEndTime) {
        this.repeatEndTime = repeatEndTime;
    }

    @Nullable
    public Integer getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(@Nullable Integer repeatCount) {
        this.repeatCount = repeatCount;
    }
}
