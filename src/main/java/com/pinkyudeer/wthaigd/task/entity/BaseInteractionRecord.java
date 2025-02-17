package com.pinkyudeer.wthaigd.task.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.annotation.Nonnull;

public abstract class BaseInteractionRecord extends BaseRecord {

    @Nonnull
    UUID fromId; // 互动发起对象
    @Nonnull
    UUID toId; // 互动接收对象

    public BaseInteractionRecord(@Nonnull UUID fromId, @Nonnull UUID toId) {
        super();
        this.fromId = fromId;
        this.toId = toId;
    }

    public BaseInteractionRecord(@Nonnull UUID fromId, @Nonnull UUID toId, @Nonnull LocalDateTime createdAt) {
        super(createdAt);
        this.fromId = fromId;
        this.toId = toId;
    }
}
