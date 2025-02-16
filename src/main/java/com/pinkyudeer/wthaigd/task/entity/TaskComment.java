package com.pinkyudeer.wthaigd.task.entity;

import java.util.UUID;

import javax.annotation.Nonnull;

public class TaskComment extends Interaction {

    @Nonnull
    private UUID taskId;
    private UUID parentCommentId; // 支持层级评论
    private String content;
}
