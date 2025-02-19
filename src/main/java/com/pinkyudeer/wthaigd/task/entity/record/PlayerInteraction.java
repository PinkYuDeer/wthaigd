package com.pinkyudeer.wthaigd.task.entity.record;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerInteraction extends BaseRecord {

    // 核心关联字段
    @Nonnull
    private InteractionType type; // 互动类型
    @Nonnull
    private UUID initiatorId; // 发起者ID
    @Nonnull
    private UUID receiverId; // 接收者ID

    // 关联实体（可选）
    @Nullable
    private UUID relatedTaskId; // 关联的任务ID
    @Nullable
    private UUID relatedCommentId; // 关联的评论ID
    @Nullable
    private UUID relatedInteractionId; // 关联的互动ID（用于回复）

    // 互动内容
    @Nullable
    private String content; // 留言/私信内容
    @Nonnull
    private InteractionStatus status = InteractionStatus.ACTIVE; // 互动状态

    // 可见性控制
    @Nonnull
    private VisibilityLevel visibility = VisibilityLevel.FRIENDS; // 可见性级别

    // 扩展信息
    @Nullable
    private String metadata; // 附加数据（如表情包位置、@位置信息等）

    public PlayerInteraction(@Nonnull InteractionType type, @Nonnull UUID initiatorId, @Nonnull UUID receiverId,
        @Nonnull UUID operatorId) {
        super(operatorId);
        this.type = type;
        this.initiatorId = initiatorId;
        this.receiverId = receiverId;
    }

    // 互动类型枚举（根据需求扩展）
    public enum InteractionType {
        FOLLOW, // 关注
        UNFOLLOW, // 取消关注
        LIKE_PROFILE, // 点赞个人主页
        COMMENT_PROFILE, // 个人主页留言
        PRIVATE_MESSAGE, // 私信
        MENTION, // @提及
        GIFT_SEND, // 赠送礼物
        FRIEND_REQUEST, // 好友请求
        REPORT // 举报玩家
    }

    // 互动状态
    public enum InteractionStatus {
        ACTIVE, // 有效
        DELETED, // 已删除
        REVOKED, // 已撤回
        ARCHIVED // 已归档
    }

    // 可见性级别（复用Task的隐私设置）
    public enum VisibilityLevel {
        PUBLIC, // 所有人可见
        TEAM, // 仅团队成员可见
        FRIENDS, // 仅好友可见
        PRIVATE // 仅自己可见
    }

    // Builder模式实现
    public static class Builder {

        private final InteractionType type;
        private final UUID initiatorId;
        private final UUID receiverId;
        private final UUID operatorId;

        // 可选参数带默认值
        private UUID recordId;
        private LocalDateTime createTime;
        private UUID relatedTaskId;
        private UUID relatedCommentId;
        private UUID relatedInteractionId;
        private String content;
        private InteractionStatus status;
        private VisibilityLevel visibility;
        private String metadata;

        public Builder(@Nonnull InteractionType type, @Nonnull UUID initiatorId, @Nonnull UUID receiverId,
            @Nonnull UUID operatorId) {
            this.type = type;
            this.initiatorId = initiatorId;
            this.receiverId = receiverId;
            this.operatorId = operatorId;
        }

        public Builder recordId(UUID val) {
            recordId = val;
            return this;
        }

        public Builder createTime(LocalDateTime val) {
            createTime = val;
            return this;
        }

        public Builder relatedTaskId(UUID val) {
            relatedTaskId = val;
            return this;
        }

        public Builder relatedCommentId(UUID val) {
            relatedCommentId = val;
            return this;
        }

        public Builder relatedInteractionId(UUID val) {
            relatedInteractionId = val;
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

        public Builder visibility(VisibilityLevel val) {
            visibility = val;
            return this;
        }

        public Builder metadata(String val) {
            metadata = val;
            return this;
        }

        public PlayerInteraction build() {
            PlayerInteraction interaction = new PlayerInteraction(type, initiatorId, receiverId, operatorId);
            if (this.recordId != null) interaction.setRecordId(this.recordId);
            if (this.createTime != null) interaction.setCreateTime(this.createTime);
            if (this.relatedTaskId != null) interaction.setRelatedTaskId(this.relatedTaskId);
            if (this.relatedCommentId != null) interaction.setRelatedCommentId(this.relatedCommentId);
            if (this.relatedInteractionId != null) interaction.setRelatedInteractionId(this.relatedInteractionId);
            if (this.content != null) interaction.setContent(this.content);
            if (this.status != null) interaction.setStatus(this.status);
            if (this.visibility != null) interaction.setVisibility(this.visibility);
            if (this.metadata != null) interaction.setMetadata(this.metadata);
            return interaction;
        }
    }

    // Getter and Setter methods
    @Nonnull
    public InteractionType getType() {
        return type;
    }

    public void setType(@Nonnull InteractionType type) {
        this.type = type;
    }

    @Nonnull
    public UUID getInitiatorId() {
        return initiatorId;
    }

    public void setInitiatorId(@Nonnull UUID initiatorId) {
        this.initiatorId = initiatorId;
    }

    @Nonnull
    public UUID getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(@Nonnull UUID receiverId) {
        this.receiverId = receiverId;
    }

    @Nullable
    public UUID getRelatedTaskId() {
        return relatedTaskId;
    }

    public void setRelatedTaskId(@Nullable UUID relatedTaskId) {
        this.relatedTaskId = relatedTaskId;
    }

    @Nullable
    public UUID getRelatedCommentId() {
        return relatedCommentId;
    }

    public void setRelatedCommentId(@Nullable UUID relatedCommentId) {
        this.relatedCommentId = relatedCommentId;
    }

    @Nullable
    public UUID getRelatedInteractionId() {
        return relatedInteractionId;
    }

    public void setRelatedInteractionId(@Nullable UUID relatedInteractionId) {
        this.relatedInteractionId = relatedInteractionId;
    }

    @Nullable
    public String getContent() {
        return content;
    }

    public void setContent(@Nullable String content) {
        this.content = content;
    }

    @Nonnull
    public InteractionStatus getStatus() {
        return status;
    }

    public void setStatus(@Nonnull InteractionStatus status) {
        this.status = status;
    }

    @Nonnull
    public VisibilityLevel getVisibility() {
        return visibility;
    }

    public void setVisibility(@Nonnull VisibilityLevel visibility) {
        this.visibility = visibility;
    }

    @Nullable
    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(@Nullable String metadata) {
        this.metadata = metadata;
    }
}
