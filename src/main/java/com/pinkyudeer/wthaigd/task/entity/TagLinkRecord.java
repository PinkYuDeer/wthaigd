package com.pinkyudeer.wthaigd.task.entity;

import java.util.UUID;

import javax.annotation.Nonnull;

public class TagLinkRecord extends BaseInteractionRecord {

    public TagLinkRecord(@Nonnull UUID fromId, @Nonnull UUID toId) {
        super(fromId, toId);
    }
}
