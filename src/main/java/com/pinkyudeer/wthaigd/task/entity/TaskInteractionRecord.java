package com.pinkyudeer.wthaigd.task.entity;

import java.util.UUID;

import javax.annotation.Nonnull;

public class TaskInteractionRecord extends BaseInteractionRecord {

    public TaskInteractionRecord(@Nonnull UUID fromId, @Nonnull UUID toId) {
        super(fromId, toId);
    }
}
