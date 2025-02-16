package com.pinkyudeer.wthaigd.task.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.annotation.Nonnull;

public abstract class Interaction {

    @Nonnull
    protected UUID id = UUID.randomUUID();
    @Nonnull
    protected UUID userId;
    protected LocalDateTime interactTime = LocalDateTime.now();
}
