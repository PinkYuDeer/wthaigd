package com.pinkyudeer.wthaigd.task.entity.record;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pinkyudeer.wthaigd.annotation.Column;
import com.pinkyudeer.wthaigd.annotation.Reference;
import com.pinkyudeer.wthaigd.annotation.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "task_interactions")
public class TaskInteraction extends BaseRecord {

    // 核心关联字段
    @Nonnull
    @Column(name = "type")
    private InteractionType type; // 互动类型
    @Nonnull
    @Column(name = "task_id")
    @Reference(referenceType = Reference.Type.TASK)
    private UUID taskId; // 关联的任务ID
    @Nonnull
    @Column(name = "player_id")
    @Reference(referenceType = Reference.Type.PLAYER)
    private UUID playerId; // 操作玩家ID

    // 关联上下文
    @Nullable
    @Column(name = "assigner_count")
    private Integer assignerCount; // 分配者数量（用于CLAIM、ASSIGN类型）
    @Nullable
    @Column(name = "comment_id")
    private UUID commentId; // 关联的评论ID, 用于回复
    @Nullable
    @Column(name = "parent_task_id")
    @Reference(referenceType = Reference.Type.TASK)
    private UUID parentTaskId; // 父任务ID（用于子任务关联）

    // 互动内容
    @Nullable
    @Column(name = "content")
    private String content; // 评论内容/举报原因等
    @Nonnull
    @Column(name = "status", defaultValue = "'ACTIVE'")
    private InteractionStatus status = InteractionStatus.ACTIVE;

    // 时间控制
    @Nullable
    @Column(name = "reminder_time")
    private LocalDateTime reminderTime; // 提醒时间（用于REMINDER类型）

    // 进度跟踪
    @Nullable
    @Column(name = "progress_percentage")
    private Integer progressPercentage; // 进度百分比（用于PROGRESS_UPDATE）

    // 扩展信息
    @Nullable
    @Column(name = "metadata")
    private String metadata; // 附加数据（如点赞位置、@信息等）

    public TaskInteraction(@Nonnull InteractionType type, @Nonnull UUID taskId, @Nonnull UUID playerId,
        @Nonnull UUID operatorId) {
        super(operatorId);
        this.type = type;
        this.taskId = taskId;
        this.playerId = playerId;
    }

    // 互动类型枚举（扩展PlayerInteraction的类型）
    public enum InteractionType {
        // 基础操作
        CLAIM, // 认领任务
        ASSIGN, // 分配任务
        FOLLOW, // 关注任务
        UNFOLLOW, // 取消关注
        LIKE, // 点赞任务
        UNLIKE, // 取消点赞
        COMMENT, // 发表评论
        REMINDER_SET, // 设置提醒
        REMINDER_CANCEL, // 取消提醒

        // 协作相关
        PROGRESS_UPDATE, // 进度更新
        SHARE, // 分享任务
        REPORT, // 举报任务
        COLLABORATE, // 协作完成
        REQUEST_HELP, // 请求协助

        // 状态变更
        ACCEPT, // 接受任务
        REJECT, // 拒绝任务
        REQUEST_DELAY, // 请求延期

        // 任务关系
        LINK_SUBTASK, // 关联子任务
        UNLINK_SUBTASK, // 解除子任务

        // 评分系统
        RATE, // 评分任务

        // 奖励相关
        REWARD_CLAIM // 领取奖励
    }

    // 互动状态（扩展基础状态）
    public enum InteractionStatus {
        PENDING, // 待处理（用于需要确认的操作）
        ACTIVE, // 有效
        DELETED, // 已删除
        REVOKED, // 已撤回
        ARCHIVED, // 已归档
        EXPIRED // 已过期（用于提醒类互动）
    }

    // Builder模式（与PlayerInteraction保持风格一致）
    public static class Builder {

        private final InteractionType type;
        private final UUID taskId;
        private final UUID playerId;
        private final UUID operatorId;

        // 可选参数
        private UUID id;
        private LocalDateTime createTime;
        private Integer assignerCount;
        private UUID commentId;
        private UUID parentTaskId;
        private String content;
        private InteractionStatus status;
        private LocalDateTime reminderTime;
        private Integer progressPercentage;
        private String metadata;

        public Builder(@Nonnull InteractionType type, @Nonnull UUID taskId, @Nonnull UUID playerId,
            @Nonnull UUID operatorId) {
            this.type = type;
            this.taskId = taskId;
            this.playerId = playerId;
            this.operatorId = operatorId;
        }

        // ... 各字段的链式方法 ...
        public Builder id(UUID val) {
            id = val;
            return this;
        }

        public Builder createTime(LocalDateTime val) {
            createTime = val;
            return this;
        }

        public Builder assignerCount(Integer val) {
            assignerCount = val;
            return this;
        }

        public Builder commentId(UUID val) {
            commentId = val;
            return this;
        }

        public Builder parentTaskId(UUID val) {
            parentTaskId = val;
            return this;
        }

        public Builder content(String val) {
            content = val;
            return this;
        }

        public Builder status(InteractionStatus val) {
            status = val;
            return this;
        }

        public Builder reminderTime(LocalDateTime val) {
            reminderTime = val;
            return this;
        }

        public Builder progressPercentage(Integer val) {
            progressPercentage = val;
            return this;
        }

        public Builder metadata(String val) {
            metadata = val;
            return this;
        }

        public TaskInteraction build() {
            TaskInteraction interaction = new TaskInteraction(type, taskId, playerId, operatorId);
            if (this.id != null) interaction.setId(this.id);
            if (this.createTime != null) interaction.setCreateTime(this.createTime);
            if (this.assignerCount != null) interaction.setAssignerCount(this.assignerCount);
            if (this.commentId != null) interaction.setCommentId(this.commentId);
            if (this.parentTaskId != null) interaction.setParentTaskId(this.parentTaskId);
            if (this.content != null) interaction.setContent(this.content);
            if (this.status != null) interaction.setStatus(this.status);
            if (this.reminderTime != null) interaction.setReminderTime(this.reminderTime);
            if (this.progressPercentage != null) interaction.setProgressPercentage(this.progressPercentage);
            if (this.metadata != null) interaction.setMetadata(this.metadata);
            return interaction;
        }
    }
}
