package com.pinkyudeer.wthaigd.task.entity.record;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.annotation.Nonnull;

public abstract class BaseRecord {

    @Nonnull
    private UUID recordId;
    @Nonnull
    private LocalDateTime createTime;
    @Nonnull
    private UUID operatorId;

    public BaseRecord(@Nonnull UUID operatorId) {
        this.recordId = UUID.randomUUID();
        this.createTime = LocalDateTime.now();
        this.operatorId = operatorId;
    }

    public BaseRecord(@Nonnull LocalDateTime createdAt, @Nonnull UUID operatorId) {
        this.recordId = UUID.randomUUID();
        this.createTime = createdAt;
        this.operatorId = operatorId;
    }

    @Nonnull
    public UUID getRecordId() {
        return recordId;
    }

    public void setRecordId(@Nonnull UUID recordId) {
        this.recordId = recordId;
    }

    @Nonnull
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(@Nonnull LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Nonnull
    public UUID getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(@Nonnull UUID operatorId) {
        this.operatorId = operatorId;
    }
}
