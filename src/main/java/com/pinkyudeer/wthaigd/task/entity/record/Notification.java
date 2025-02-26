package com.pinkyudeer.wthaigd.task.entity.record;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pinkyudeer.wthaigd.annotation.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;

// ... existing package and imports ...
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "notifications")
public class Notification extends BaseRecord {

    // 基础属性
    @Nonnull
    private NotificationType type; // 通知类型（枚举）
    @Nonnull
    private String title; // 通知标题
    @Nullable
    private String content; // 详细内容（可包含富文本）
    @Nonnull
    private NotificationStatus notificationStatus = NotificationStatus.UNREAD; // 通知状态

    // 关联属性
    @Nonnull
    private UUID receiverId; // 接收者ID（Player/TEAM）
    @Nullable
    private UUID triggerPlayerId; // 触发通知的玩家
    @Nullable
    private UUID relatedTaskId; // 关联的任务ID
    @Nullable
    private UUID relatedTeamId; // 关联的团队ID
    @Nullable
    private UUID relatedRecordId; // 关联的记录ID（如互动记录）

    // 时间属性
    @Nullable
    private LocalDateTime expireTime; // 过期时间（临时通知）
    @Nullable
    private LocalDateTime readTime; // 阅读时间

    // 统计与追踪
    @Nonnull
    private SourceType sourceType; // 通知来源
    @Nonnull
    private NotificationPriority priority = NotificationPriority.NORMAL; // 优先级
    @Nullable
    private String actionType; // 触发动作类型（如"TASK_ASSIGN"）

    // 界面相关
    @Nullable
    private String jumpLink; // 跳转链接（如任务详情页）
    @Nonnull
    private RelatedEntityType relatedEntityType = RelatedEntityType.DEFAULT; // 关联实体类型
    @Nullable
    private String categoryTag; // 分类标签

    public Notification(@Nonnull NotificationType type, @Nonnull String title, @Nonnull UUID receiverId,
        @Nonnull SourceType sourceType, @Nonnull UUID operatorId) {
        super(operatorId);
        this.type = type;
        this.title = title;
        this.receiverId = receiverId;
        this.sourceType = sourceType;
    }

    public static class Builder {

        private final NotificationType type;
        private final String title;
        private final UUID receiverId;
        private final SourceType sourceType;
        private final UUID operatorId;

        private UUID id;
        private NotificationStatus status;
        private LocalDateTime createTime;
        private NotificationPriority priority;
        private RelatedEntityType relatedEntityType;
        private UUID relatedTeamId;
        private UUID relatedRecordId;
        private LocalDateTime expireTime;
        private LocalDateTime readTime;
        private String actionType;
        private String jumpLink;
        private String categoryTag;
        private String content;
        private UUID triggerPlayerId;
        private UUID relatedTaskId;

        public Builder(@Nonnull NotificationType type, @Nonnull String title, @Nonnull UUID receiverId,
            @Nonnull SourceType sourceType, @Nonnull UUID operatorId) {
            this.type = type;
            this.title = title;
            this.receiverId = receiverId;
            this.sourceType = sourceType;
            this.operatorId = operatorId;
        }

        public Builder id(UUID val) {
            id = val;
            return this;
        }

        public Builder status(NotificationStatus val) {
            status = val;
            return this;
        }

        public Builder createTime(LocalDateTime val) {
            createTime = val;
            return this;
        }

        public Builder priority(NotificationPriority val) {
            priority = val;
            return this;
        }

        public Builder relatedEntityType(RelatedEntityType val) {
            relatedEntityType = val;
            return this;
        }

        public Builder content(String val) {
            content = val;
            return this;
        }

        public Builder triggerPlayerId(UUID val) {
            triggerPlayerId = val;
            return this;
        }

        public Builder relatedTaskId(UUID val) {
            relatedTaskId = val;
            return this;
        }

        public Builder relatedTeamId(UUID val) {
            relatedTeamId = val;
            return this;
        }

        public Builder relatedRecordId(UUID val) {
            relatedRecordId = val;
            return this;
        }

        public Builder expireTime(LocalDateTime val) {
            expireTime = val;
            return this;
        }

        public Builder readTime(LocalDateTime val) {
            readTime = val;
            return this;
        }

        public Builder actionType(String val) {
            actionType = val;
            return this;
        }

        public Builder jumpLink(String val) {
            jumpLink = val;
            return this;
        }

        public Builder categoryTag(String val) {
            categoryTag = val;
            return this;
        }

        public Notification build() {
            Notification notification = new Notification(type, title, receiverId, sourceType, operatorId);
            if (this.id != null) notification.setId(this.id);
            if (this.status != null) notification.setNotificationStatus(this.status);
            if (this.createTime != null) notification.setCreateTime(this.createTime);
            if (this.priority != null) notification.setPriority(this.priority);
            if (this.relatedEntityType != null) notification.setRelatedEntityType(this.relatedEntityType);
            if (this.content != null) notification.setContent(this.content);
            if (this.triggerPlayerId != null) notification.setTriggerPlayerId(this.triggerPlayerId);
            if (this.relatedTaskId != null) notification.setRelatedTaskId(this.relatedTaskId);
            if (this.relatedTeamId != null) notification.setRelatedTeamId(this.relatedTeamId);
            if (this.relatedRecordId != null) notification.setRelatedRecordId(this.relatedRecordId);
            if (this.expireTime != null) notification.setExpireTime(this.expireTime);
            if (this.readTime != null) notification.setReadTime(this.readTime);
            if (this.actionType != null) notification.setActionType(this.actionType);
            if (this.jumpLink != null) notification.setJumpLink(this.jumpLink);
            if (this.categoryTag != null) notification.setCategoryTag(this.categoryTag);
            return notification;
        }
    }

    // 枚举定义
    public enum NotificationType {
        TASK_ASSIGNED, // 任务分配
        STATUS_CHANGE, // 状态变更
        TEAM_INVITATION, // 团队邀请
        TASK_REMINDER, // 任务提醒
        SYSTEM_ALERT, // 系统警报
        SOCIAL_INTERACTION, // 社交互动（点赞/评论）
        APPROVAL_REQUEST, // 审批请求
        POINTS_UPDATE // 积分变动
    }

    public enum NotificationPriority {
        EMERGENCY, // 紧急（需要立即处理）
        IMPORTANT, // 重要
        NORMAL, // 普通
        LOW // 低优先级
    }

    public enum SourceType {
        SYSTEM, // 系统生成
        PLAYER, // 玩家自己触发
        TEAM // 团队成员触发
    }

    // 关联实体类型（用于界面跳转时识别）
    public enum RelatedEntityType {
        TASK,
        TEAM,
        PLAYER,
        TAG,
        RECORD,
        COMMENT,
        DEFAULT
    }

    // 状态枚举（扩展用）
    public enum NotificationStatus {
        UNREAD, // 未读
        READ, // 已读
        ARCHIVED, // 已归档
        PROCESSED, // 已处理
        EXPIRED, // 已过期
        DELETED // 已删除
    }

}
