package com.pinkyudeer.wthaigd.task.entity.record;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pinkyudeer.wthaigd.task.entity.Task;
import com.pinkyudeer.wthaigd.task.entity.record.Notification.SourceType;

public class StatusChangeRecord extends BaseRecord {

    // 核心状态变更信息
    @Nonnull
    private UUID taskId; // 关联的任务ID
    @Nonnull
    private Task.TaskStatus oldStatus; // 变更前状态
    @Nonnull
    private Task.TaskStatus newStatus; // 变更后状态

    // 变更上下文信息
    @Nullable
    private String reason; // 状态变更原因（可选）
    @Nonnull
    private Boolean isAutomatic = false; // 是否自动触发（如超时自动关闭）
    @Nullable
    private UUID relatedTeamId; // 关联团队ID（当变更涉及团队操作时）

    // 变更来源追踪
    @Nonnull
    private SourceType sourceType = SourceType.SYSTEM; // 变更来源（复用Notification的SourceType）

    // 扩展信息（可存储JSON格式的附加数据）
    @Nullable
    private String metadata; // 附加信息（如审批流程ID、阻塞原因等）

    public StatusChangeRecord(@Nonnull UUID operatorId, @Nonnull UUID taskId, @Nonnull Task.TaskStatus oldStatus,
        @Nonnull Task.TaskStatus newStatus) {
        super(operatorId);
        this.taskId = taskId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }

    // 使用Builder模式创建记录
    public static class Builder {

        private final UUID taskId;
        private final Task.TaskStatus oldStatus;
        private final Task.TaskStatus newStatus;
        private final UUID operatorId;

        // 可选参数带默认值
        private UUID recordId;
        private LocalDateTime createTime;
        private String reason;
        private Boolean isAutomatic;
        private UUID relatedTeamId;
        private SourceType sourceType;
        private String metadata;

        public Builder(@Nonnull UUID taskId, @Nonnull Task.TaskStatus oldStatus, @Nonnull Task.TaskStatus newStatus,
            @Nonnull UUID operatorId) {
            this.taskId = taskId;
            this.oldStatus = oldStatus;
            this.newStatus = newStatus;
            this.operatorId = operatorId;
        }

        public Builder reason(String val) {
            reason = val;
            return this;
        }

        public Builder isAutomatic(Boolean val) {
            isAutomatic = val;
            return this;
        }

        public Builder relatedTeamId(UUID val) {
            relatedTeamId = val;
            return this;
        }

        public Builder sourceType(SourceType val) {
            sourceType = val;
            return this;
        }

        public Builder metadata(String val) {
            metadata = val;
            return this;
        }

        public StatusChangeRecord build() {
            StatusChangeRecord record = new StatusChangeRecord(
                this.operatorId,
                this.taskId,
                this.oldStatus,
                this.newStatus);
            if (this.recordId != null) record.setRecordId(this.recordId);
            if (this.createTime != null) record.setCreateTime(this.createTime);
            if (this.reason != null) record.setReason(this.reason);
            if (this.isAutomatic != null) record.setIsAutomatic(this.isAutomatic);
            if (this.relatedTeamId != null) record.setRelatedTeamId(this.relatedTeamId);
            if (this.sourceType != null) record.setSourceType(this.sourceType);
            if (this.metadata != null) record.setMetadata(this.metadata);
            return record;
        }
    }

    // Getter and Setter methods
    @Nonnull
    public UUID getTaskId() {
        return taskId;
    }

    public void setTaskId(@Nonnull UUID taskId) {
        this.taskId = taskId;
    }

    @Nonnull
    public Task.TaskStatus getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(@Nonnull Task.TaskStatus oldStatus) {
        this.oldStatus = oldStatus;
    }

    @Nonnull
    public Task.TaskStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(@Nonnull Task.TaskStatus newStatus) {
        this.newStatus = newStatus;
    }

    @Nullable
    public String getReason() {
        return reason;
    }

    public void setReason(@Nullable String reason) {
        this.reason = reason;
    }

    @Nonnull
    public Boolean getIsAutomatic() {
        return isAutomatic;
    }

    public void setIsAutomatic(@Nonnull Boolean isAutomatic) {
        this.isAutomatic = isAutomatic;
    }

    @Nullable
    public UUID getRelatedTeamId() {
        return relatedTeamId;
    }

    public void setRelatedTeamId(@Nullable UUID relatedTeamId) {
        this.relatedTeamId = relatedTeamId;
    }

    @Nonnull
    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(@Nonnull SourceType sourceType) {
        this.sourceType = sourceType;
    }

    @Nullable
    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(@Nullable String metadata) {
        this.metadata = metadata;
    }
}
