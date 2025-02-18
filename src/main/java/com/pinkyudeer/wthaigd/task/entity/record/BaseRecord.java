package com.pinkyudeer.wthaigd.task.entity.record;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.annotation.Nonnull;

public abstract class BaseRecord {

    @Nonnull
    protected UUID recordId;
    @Nonnull
    protected LocalDateTime createTime;
    @Nonnull
    protected UUID operatorId;

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
}
