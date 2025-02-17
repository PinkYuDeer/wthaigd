package com.pinkyudeer.wthaigd.task.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

public class User {

    @Nonnull
    private UUID id;
    @Nonnull
    private String username;
    private String displayName;
    @Nonnull
    private LocalDateTime registerTime = LocalDateTime.now();
    private LocalDateTime lastLoginTime;

    // 社交统计
    private int followerCount;
    private int followingCount;
    private int receivedLikesCount;

    // 关联属性（需要懒加载）
    private List<UUID> assignedTasks = new ArrayList<>();
}
