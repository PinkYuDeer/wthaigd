package com.pinkyudeer.wthaigd.task.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.annotation.Nonnull;

public class TaskAssignment {

    @Nonnull
    private UUID id = UUID.randomUUID();
    @Nonnull
    private UUID taskId;
    private UUID assignerId; // 分配者（可空表示系统分配）
    @Nonnull
    private UUID assigneeId; // 执行者
    @Nonnull
    private AssignmentStatus status = AssignmentStatus.PENDING;
    private LocalDateTime assignTime = LocalDateTime.now();
    private LocalDateTime acceptTime;
    private LocalDateTime completeTime;

    public enum AssignmentStatus {
        PENDING, // 待接受
        ACCEPTED, // 已接受
        IN_PROGRESS, // 进行中
        COMPLETED, // 已完成
        REJECTED // 已拒绝
    }
}
