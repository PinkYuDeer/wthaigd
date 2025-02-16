package com.pinkyudeer.wthaigd.task.entity;

import java.util.UUID;

import javax.annotation.Nonnull;

// 用户互动
public class UserFollow extends Interaction {

    @Nonnull
    private UUID targetUserId;
}
