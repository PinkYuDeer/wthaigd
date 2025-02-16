package com.pinkyudeer.wthaigd.task.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.annotation.Nonnull;

public class StatusChangeRecord {

    @Nonnull
    private UUID taskId;
    private UUID changerId; // 可空表示系统变更
    @Nonnull
    private Task.TaskStatus fromStatus;
    @Nonnull
    private Task.TaskStatus toStatus;
    @Nonnull
    private LocalDateTime changeTime = LocalDateTime.now();
    private String changeReason;
}
