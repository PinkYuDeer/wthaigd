package com.pinkyudeer.wthaigd.task.entity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Task {

    // task 基础属性
    @Nonnull
    public String id = UUID.randomUUID()
        .toString(); // 任务的唯一标识符，用 UUID
    @Nonnull
    public String title; // 任务标题
    @Nonnull
    public String description; // 任务描述
    @Nonnull
    public Integer version = 0; // 修订次数
    @Nonnull
    public PrivacyLevel visibility = PrivacyLevel.PRIVATE; // 隐私级别（枚举类型）
    @Nullable
    public TaskType taskType; // 任务类型

    // task 参与者属性
    @Nonnull
    public UUID creator; // 创建者
    @Nullable
    public Integer assigneeCount; // 负责人个数
    @Nullable
    public UUID teamId; // 所属团队ID
    @Nonnull
    public Integer followerCount = 0; // 关注者数量
    @Nonnull
    public Integer likeCount = 0; // 点赞数量
    @Nonnull
    public Integer commentCount = 0; // 评论数量
    @Nullable
    public UUID lastOperator; // 最后操作人

    // task 状态属性
    @Nonnull
    public Priority priority; // 优先级（枚举类型）
    @Nonnull
    public Importance importance = Importance.UNDEFINED; // 重要程度（枚举类型）
    @Nonnull
    public Urgency urgency = Urgency.UNDEFINED; // 紧急程度（枚举类型）
    @Nonnull
    public TaskStatus status = TaskStatus.UnClaimed; // 状态（枚举类型）

    // task 时间属性
    @Nonnull
    public LocalDateTime createTime = LocalDateTime.now(); // 创建时间
    @Nullable
    public LocalDateTime startTime; // 开始时间.
    @Nullable
    public LocalDateTime endTime; // 完成时间
    @Nonnull
    public LocalDateTime updateTime = LocalDateTime.now(); // 更新时间
    @Nullable
    public LocalDateTime deadline; // 截止时间
    @Nullable
    public Duration estimatedDuration; // 预估耗时
    @Nullable
    public Duration actualDuration; // 实际耗时
    @Nullable
    public LocalDateTime reminderTime; // 提醒时间
    @Nullable
    public String recurrenceRule; // 国际标准的RRule,例如"FREQ=WEEKLY;INTERVAL=2;COUNT=5"
    @Nullable
    public LocalDateTime repeatTime; // 重复时间
    @Nullable
    public Duration repeatInterval; // 重复时间间隔
    @Nullable
    public LocalDateTime repeatEndTime; // 重复结束时间
    @Nullable
    public Integer repeatCount; // 重复次数

    // 优化优先级计算逻辑（添加缓存机制）
    private transient Priority cachedPriority;

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

    public Priority getPriority() {
        if (cachedPriority == null) {
            cachedPriority = calculatePriority(importance, urgency);
        }
        return cachedPriority;
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
}
