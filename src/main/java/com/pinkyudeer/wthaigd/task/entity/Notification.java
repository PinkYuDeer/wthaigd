package com.pinkyudeer.wthaigd.task.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.annotation.Nonnull;

public class Notification {

    @Nonnull
    private UUID id = UUID.randomUUID();
    @Nonnull
    private NotificationType type; // 枚举类型
    private UUID receiverId;
    private UUID relatedTaskId; // 可空
    private UUID relatedUserId; // 可空
    @Nonnull
    private LocalDateTime createTime = LocalDateTime.now();
    private boolean acknowledged;
    private String fingerprint; // 去重指纹（类型+目标ID+触发者ID的哈希）

    public enum NotificationType {
        TASK_ASSIGNED,
        TASK_COMMENTED,
        TASK_LIKED,
        TASK_FOLLOWED,
        TASK_STATUS_CHANGED,
        USER_MENTIONED
    }
}
