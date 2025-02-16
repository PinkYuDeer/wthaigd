package com.pinkyudeer.wthaigd.task.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

public class Team {

    @Nonnull
    private UUID id = UUID.randomUUID();
    @Nonnull
    private String teamName;
    private UUID creatorId;
    private List<UUID> members = new ArrayList<>();
    private List<UUID> teamTasks = new ArrayList<>();
    @Nonnull
    private LocalDateTime createTime = LocalDateTime.now();
    private boolean isActive = true;
}
