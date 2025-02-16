package com.pinkyudeer.wthaigd.task.entity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Task {

    // task 基础属性
    @Nonnull
    public String id; // 任务的唯一标识符，用 UUID
    @Nonnull
    public String title; // 任务标题
    @Nonnull
    public String description; // 任务描述
    @Nonnull
    public Integer version; // 修订次数
    @Nonnull
    public PrivacyLevel visibility; // 隐私级别（枚举类型）
    @Nullable
    public String taskType; // 任务类型（日常/主线/活动）

    // task 参与者属性
    @Nullable
    public String assignee; // 负责人
    @Nonnull
    public Integer followerCount; // 关注者数量
    @Nonnull
    public Integer likeCount; // 点赞数量
    @Nonnull
    public Integer commentCount; // 评论数量
    @Nullable
    public String lastOperator; // 最后操作人

    // task 状态属性
    @Nonnull
    public Priority priority; // 优先级（枚举类型）
    @Nonnull
    public Importance importance; // 重要程度（枚举类型）
    @Nonnull
    public Urgency urgency; // 紧急程度（枚举类型）
    @Nonnull
    public TaskStatus status; // 状态（枚举类型）

    // task 时间属性
    @Nonnull
    public LocalDateTime createTime; // 创建时间
    @Nullable
    public LocalDateTime startTime; // 开始时间.
    @Nullable
    public LocalDateTime endTime; // 完成时间
    @Nonnull
    public LocalDateTime updateTime; // 更新时间
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

    // // task 关联属性
    // @Nullable
    // private List<String> dependencies; // 任务依赖列表
    // @Nullable
    // private List<String> tags; // 标签

    // 构造函数
    public Task(String title, String description) {
        this.id = UUID.randomUUID()
            .toString();
        this.title = title;
        this.description = description;
        this.version = 0;
        this.visibility = PrivacyLevel.PRIVATE;
        this.followerCount = 0;
        this.likeCount = 0;
        this.commentCount = 0;
        this.importance = Importance.UNDEFINED;
        this.urgency = Urgency.UNDEFINED;
        this.priority = getPriority(importance, urgency);
        this.status = TaskStatus.UnClaimed;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    // 带有重要程度和紧急程度的构造函数
    public Task(String title, String description, Importance importance, Urgency urgency) {
        this(title, description);
        this.importance = importance;
        this.urgency = urgency;
        this.priority = getPriority(importance, urgency);
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

    public static Priority getPriority(Importance importance, Urgency urgency) {
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

        // 默认优先级
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

}
