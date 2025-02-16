package com.pinkyudeer.wthaigd.task.entity;

import java.util.UUID;

import javax.annotation.Nonnull;

public class Tag {

    @Nonnull
    private UUID id = UUID.randomUUID();
    @Nonnull
    private String tagName;
    private String colorHex;
    private int usageCount;
    private UUID creatorId; // null表示系统预定义标签
}
