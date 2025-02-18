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
    public UUID taskId; // 关联的任务ID
    @Nonnull
    public Task.TaskStatus oldStatus; // 变更前状态
    @Nonnull
    public Task.TaskStatus newStatus; // 变更后状态

    // 变更上下文信息
    @Nullable
    public String reason; // 状态变更原因（可选）
    @Nonnull
    public Boolean isAutomatic = false; // 是否自动触发（如超时自动关闭）
    @Nullable
    public UUID relatedTeamId; // 关联团队ID（当变更涉及团队操作时）

    // 变更来源追踪
    @Nonnull
    public SourceType sourceType = SourceType.SYSTEM; // 变更来源（复用Notification的SourceType）

    // 扩展信息（可存储JSON格式的附加数据）
    @Nullable
    public String metadata; // 附加信息（如审批流程ID、阻塞原因等）

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
        private String reason;
        private Boolean isAutomatic = false;
        private UUID relatedTeamId;
        private SourceType sourceType = SourceType.SYSTEM;
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
            record.recordId = UUID.randomUUID();
            record.createTime = LocalDateTime.now();
            record.taskId = this.taskId;
            record.oldStatus = this.oldStatus;
            record.newStatus = this.newStatus;
            record.operatorId = this.operatorId;
            record.reason = this.reason;
            record.isAutomatic = this.isAutomatic;
            record.relatedTeamId = this.relatedTeamId;
            record.sourceType = this.sourceType;
            record.metadata = this.metadata;
            return record;
        }
    }
}
