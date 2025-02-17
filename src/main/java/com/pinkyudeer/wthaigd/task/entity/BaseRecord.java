package com.pinkyudeer.wthaigd.task.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.annotation.Nonnull;

public abstract class BaseRecord {

    @Nonnull
    protected final UUID recordId;
    @Nonnull
    protected final LocalDateTime createTime;

    public BaseRecord() {
        this.recordId = UUID.randomUUID();
        this.createTime = LocalDateTime.now();
    }

    public BaseRecord(@Nonnull LocalDateTime createdAt) {
        this.recordId = UUID.randomUUID();
        this.createTime = createdAt;
    }
}
