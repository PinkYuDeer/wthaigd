package com.pinkyudeer.wthaigd.task.entity;

import java.util.UUID;

import javax.annotation.Nonnull;

public class TaskLike extends Interaction {

    @Nonnull
    private UUID taskId;
    @Nonnull
    private String likeType; // 点赞类型（普通/超级赞）
}
