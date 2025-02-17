package com.pinkyudeer.wthaigd.task.entity;

import java.util.UUID;

import javax.annotation.Nonnull;

public class TeamMemberRecord extends BaseInteractionRecord {

    public TeamMemberRecord(@Nonnull UUID fromId, @Nonnull UUID toId) {
        super(fromId, toId);
    }
}
