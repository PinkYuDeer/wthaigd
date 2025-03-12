package com.pinkyudeer.wthaigd.task.entity.record;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pinkyudeer.wthaigd.helper.dataBase.annotation.Column;
import com.pinkyudeer.wthaigd.helper.dataBase.annotation.Reference;
import com.pinkyudeer.wthaigd.helper.dataBase.annotation.Table;
import com.pinkyudeer.wthaigd.task.entity.Task;
import com.pinkyudeer.wthaigd.task.entity.Team;
import com.pinkyudeer.wthaigd.task.entity.record.Notification.SourceType;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "task_histories")
public class StatusChangeRecord extends BaseRecord {

    // 核心状态变更信息
    @Nonnull
    @Column(name = "task_id")
    @Reference(entity = Task.class)
    private UUID taskId; // 关联的任务ID
    @Nonnull
    @Column(name = "old_status")
    private Task.TaskStatus oldStatus; // 变更前状态
    @Nonnull
    @Column(name = "new_status")
    private Task.TaskStatus newStatus; // 变更后状态

    // 变更上下文信息
    @Nullable
    @Column(name = "reason")
    private String reason; // 状态变更原因（可选）
    @Nonnull
    @Column(name = "is_automatic", defaultValue = "false")
    private Boolean isAutomatic = false; // 是否自动触发（如超时自动关闭）
    @Nullable
    @Column(name = "related_team_id")
    @Reference(entity = Team.class)
    private UUID relatedTeamId; // 关联团队ID（当变更涉及团队操作时）

    // 变更来源追踪
    @Nonnull
    @Column(name = "source_type")
    private SourceType sourceType = SourceType.SYSTEM; // 变更来源（复用Notification的SourceType）

    // 扩展信息（可存储JSON格式的附加数据）
    @Nullable
    @Column(name = "metadata")
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
        private UUID id;
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
            if (this.id != null) record.setId(this.id);
            if (this.createTime != null) record.setCreateTime(this.createTime);
            if (this.reason != null) record.setReason(this.reason);
            if (this.isAutomatic != null) record.setIsAutomatic(this.isAutomatic);
            if (this.relatedTeamId != null) record.setRelatedTeamId(this.relatedTeamId);
            if (this.sourceType != null) record.setSourceType(this.sourceType);
            if (this.metadata != null) record.setMetadata(this.metadata);
            return record;
        }
    }
}
