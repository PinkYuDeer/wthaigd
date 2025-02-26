package com.pinkyudeer.wthaigd.task.entity.record;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pinkyudeer.wthaigd.task.entity.Task;
import com.pinkyudeer.wthaigd.task.entity.record.Notification.RelatedEntityType;
import com.pinkyudeer.wthaigd.task.entity.record.Notification.SourceType;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TagLink extends BaseRecord {

    // 核心关联字段
    @Nonnull
    private UUID tagId; // 关联的标签ID
    @Nonnull
    private RelatedEntityType entityType; // 关联实体类型（复用Notification的枚举）
    @Nonnull
    private UUID entityId; // 关联的实体ID（任务/玩家/团队等）

    // 上下文信息
    @Nonnull
    private SourceType sourceType = SourceType.SYSTEM; // 关联来源（复用Notification的SourceType）

    // 权限控制
    @Nonnull
    private Task.PrivacyLevel visibility = Task.PrivacyLevel.TEAM; // 可见性设置

    // 扩展信息
    @Nullable
    private String metadata; // 附加信息（JSON格式存储额外数据）
    @Nonnull
    private Boolean isActive = true; // 关联是否有效（软删除标志）

    public TagLink(@Nonnull UUID tagId, @Nonnull RelatedEntityType entityType, @Nonnull UUID entityId,
        @Nonnull UUID operatorId) {
        super(operatorId);
        this.tagId = tagId;
        this.entityType = entityType;
        this.entityId = entityId;
    }

    public static class Builder {

        private final UUID operatorId;
        private final UUID tagId;
        private final RelatedEntityType entityType;
        private final UUID entityId;

        // 可选参数带默认值
        private UUID id;
        private LocalDateTime createTime;
        private SourceType sourceType;
        private Task.PrivacyLevel visibility;
        private String metadata;
        private Boolean isActive;

        public Builder(@Nonnull UUID tagId, @Nonnull RelatedEntityType entityType, @Nonnull UUID entityId,
            @Nonnull UUID operatorId) {
            this.operatorId = operatorId;
            this.tagId = tagId;
            this.entityType = entityType;
            this.entityId = entityId;
        }

        public Builder id(UUID val) {
            id = val;
            return this;
        }

        public Builder createTime(LocalDateTime val) {
            createTime = val;
            return this;
        }

        public Builder sourceType(SourceType val) {
            sourceType = val;
            return this;
        }

        public Builder visibility(Task.PrivacyLevel val) {
            visibility = val;
            return this;
        }

        public Builder metadata(String val) {
            metadata = val;
            return this;
        }

        public Builder isActive(Boolean val) {
            isActive = val;
            return this;
        }

        public TagLink build() {
            TagLink link = new TagLink(tagId, entityType, entityId, operatorId);
            if (this.id != null) link.setId(this.id);
            if (this.createTime != null) link.setCreateTime(this.createTime);
            if (this.sourceType != null) link.setSourceType(this.sourceType);
            if (this.visibility != null) link.setVisibility(this.visibility);
            if (this.metadata != null) link.setMetadata(this.metadata);
            if (this.isActive != null) link.setIsActive(this.isActive);
            return link;
        }
    }
}
