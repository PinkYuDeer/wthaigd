package com.pinkyudeer.wthaigd.task.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Task {

    private String id; // 任务的唯一标识符，用 UUID
    private String title; // 任务标题
    private String description; // 任务描述
    private String assignee; // 负责人
    private List<String> followers; // 关注者
    private List<String> likes; // 点赞
    private List<String> comments; // 评论
    private Priority priority; // 优先级（枚举类型）
    private Importance importance; // 重要程度（枚举类型）
    private Urgency urgency; // 紧急程度（枚举类型）
    private Status status; // 状态（枚举类型）
    private long createTime; // 创建时间（时间戳）
    private long startTime; // 开始时间（时间戳）
    private long endTime; // 完成时间（时间戳）
    private long updateTime; // 更新时间（时间戳）
    private List<String> subtasks; // 子任务的ID
    private List<String> tags; // 标签

    // 构造函数
    public Task(String title, String description) {
        this.id = UUID.randomUUID()
            .toString();
        this.title = title;
        this.description = description;
        this.assignee = null;
        this.followers = new ArrayList<>();
        this.likes = new ArrayList<>();
        this.comments = new ArrayList<>();
        this.priority = Priority.UNDEFINED;
        this.importance = Importance.UNDEFINED;
        this.urgency = Urgency.UNDEFINED;
        this.status = Status.UnClaimed;
        this.createTime = System.currentTimeMillis();
        this.startTime = 0;
        this.endTime = 0;
        this.updateTime = System.currentTimeMillis();
        this.subtasks = new ArrayList<>();
        this.tags = new ArrayList<>();
    }

    // 带有重要程度和紧急程度的构造函数
    public Task(String title, String description, Importance importance, Urgency urgency) {
        this(title, description);
        this.importance = importance;
        this.urgency = urgency;
        this.priority = getPriority(importance, urgency);
    }

    // 优雅打印任务信息（方便调试）
    @Override
    public String toString() {
        return "Task{" + "id='"
            + id
            + '\''
            + ", title='"
            + title
            + '\''
            + ", description='"
            + description
            + '\''
            + ", assignee='"
            + assignee
            + '\''
            + ", followers="
            + followers.toString()
            + ", likes="
            + likes.toString()
            + ", comments="
            + comments.toString()
            + ", priority="
            + priority
            + ", importance="
            + importance
            + ", urgency="
            + urgency
            + ", status="
            + status
            + ", createTime="
            + createTime
            + ", startTime="
            + startTime
            + ", endTime="
            + endTime
            + ", updateTime="
            + updateTime
            + ", subtasks="
            + subtasks.toString()
            + ", tags="
            + tags.toString()
            + '}';
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

    // 状态
    public enum Status {
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

    // Getter 和 Setter 方法
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public List<String> getFollowers() {
        return followers;
    }

    public void setFollowers(List<String> followers) {
        this.followers = followers;
    }

    public List<String> getLikes() {
        return likes;
    }

    public void setLikes(List<String> likes) {
        this.likes = likes;
    }

    public List<String> getComments() {
        return comments;
    }

    public void setComments(List<String> comments) {
        this.comments = comments;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Importance getImportance() {
        return importance;
    }

    public void setImportance(Importance importance) {
        this.importance = importance;
    }

    public Urgency getUrgency() {
        return urgency;
    }

    public void setUrgency(Urgency urgency) {
        this.urgency = urgency;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public List<String> getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(List<String> subtasks) {
        this.subtasks = subtasks;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getName() {
        return title;
    }
}
