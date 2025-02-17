package com.pinkyudeer.wthaigd.task.entity;

import java.util.UUID;

import javax.annotation.Nonnull;

public class TeamRequestRecord extends BaseInteractionRecord {

    public TeamRequestRecord(@Nonnull UUID fromId, @Nonnull UUID toId) {
        super(fromId, toId);
    }
}
