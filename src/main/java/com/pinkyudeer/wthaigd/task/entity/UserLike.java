package com.pinkyudeer.wthaigd.task.entity;

import java.util.UUID;

import javax.annotation.Nonnull;

public class UserLike extends Interaction {

    @Nonnull
    private UUID targetUserId;
    private String likeType;
}
