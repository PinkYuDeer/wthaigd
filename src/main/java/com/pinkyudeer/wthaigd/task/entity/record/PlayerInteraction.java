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
@Table(name = "player_interactions")
public class PlayerInteraction extends BaseRecord {

    // 核心关联字段
    @Nonnull
    @Column(name = "type")
    private InteractionType type; // 互动类型
    @Nonnull
    @Column(name = "initiator_id")
    @Reference(referenceType = Reference.Type.PLAYER)
    private UUID initiatorId; // 发起者ID
    @Nonnull
    @Column(name = "receiver_id")
    @Reference(referenceType = Reference.Type.PLAYER)
    private UUID receiverId; // 接收者ID

    // 关联实体（可选）
    @Nullable
    @Reference(referenceType = Reference.Type.TASK)
    @Column(name = "related_task_id")
    private UUID relatedTaskId; // 关联的任务ID
    @Nullable
    @Column(name = "related_record_id")
    private UUID relatedRecordId; // 关联的互动ID

    // 互动内容
    @Nullable
    @Column(name = "content")
    private String content; // 留言/私信内容
    @Nonnull
    @Column(name = "status", defaultValue = "'ACTIVE'")
    private InteractionStatus status = InteractionStatus.ACTIVE; // 互动状态

    // 可见性控制
    @Nonnull
    @Column(name = "visibility", defaultValue = "'FRIENDS'")
    private VisibilityLevel visibility = VisibilityLevel.FRIENDS; // 可见性级别

    // 扩展信息
    @Nullable
    @Column(name = "metadata")
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
        private UUID id;
        private LocalDateTime createTime;
        private UUID relatedTaskId;
        private UUID relatedRecordId;
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

        public Builder id(UUID val) {
            id = val;
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

        public Builder relatedRecordId(UUID val) {
            relatedRecordId = val;
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
            if (this.id != null) interaction.setId(this.id);
            if (this.createTime != null) interaction.setCreateTime(this.createTime);
            if (this.relatedTaskId != null) interaction.setRelatedTaskId(this.relatedTaskId);
            if (this.relatedRecordId != null) interaction.setRelatedRecordId(this.relatedRecordId);
            if (this.content != null) interaction.setContent(this.content);
            if (this.status != null) interaction.setStatus(this.status);
            if (this.visibility != null) interaction.setVisibility(this.visibility);
            if (this.metadata != null) interaction.setMetadata(this.metadata);
            return interaction;
        }
    }
}
