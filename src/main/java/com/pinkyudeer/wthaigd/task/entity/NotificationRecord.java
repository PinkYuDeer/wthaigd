package com.pinkyudeer.wthaigd.task.entity;

import java.util.UUID;

import javax.annotation.Nonnull;

public class NotificationRecord extends BaseInteractionRecord {

    public NotificationRecord(@Nonnull UUID fromId, @Nonnull UUID toId) {
        super(fromId, toId);
    }
}
