package com.pinkyudeer.wthaigd.task.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pinkyudeer.wthaigd.annotation.Table;

import lombok.Data;

@Data
@Table(name = "tags")
public class Tag {

    // 基础属性
    @Nonnull
    private UUID id = UUID.randomUUID(); // 标签唯一标识符
    @Nonnull
    private String name; // 标签名称
    @Nullable
    private String description; // 标签描述
    @Nonnull
    private String colorCode = "#FFFFFF"; // 标签颜色
    @Nonnull
    private String fontColorCode = "#000000"; // 标签字体颜色
    @Nonnull
    private Boolean isDefault = false; // 是否为默认标签

    // 关联统计
    @Nonnull
    private transient Integer linkedTaskCount = 0; // 关联任务数
    @Nonnull
    private transient Integer linkedTeamCount = 0; // 关联团队数
    @Nonnull
    private transient Integer linkedPlayerCount = 0; // 关联玩家数

    // 权限控制
    @Nonnull
    private TagScope scope = TagScope.PUBLIC; // 标签可见范围
    @Nullable
    private UUID ownerId; // 私有标签拥有者

    // 时间戳
    @Nonnull
    private LocalDateTime createTime = LocalDateTime.now(); // 创建时间
    @Nonnull
    private LocalDateTime updateTime = LocalDateTime.now(); // 更新时间

    public enum TagScope {
        SYSTEM, // 系统预置标签
        PUBLIC, // 公开标签
        TEAM, // 团队内部标签
        PRIVATE // 用户私有标签
    }

    public Tag(@Nonnull String name, @Nullable String description) {
        this.name = name;
        if ("".equals(description)) {
            this.description = null;
        } else {
            this.description = description;
        }
    }

    public Tag(@Nonnull String name, @Nullable String description, @Nonnull String colorCode) {
        this(name, description);
        this.colorCode = colorCode;
        // 自动计算字体颜色
        int r = Integer.parseInt(colorCode.substring(1, 3), 16);
        int g = Integer.parseInt(colorCode.substring(3, 5), 16);
        int b = Integer.parseInt(colorCode.substring(5, 7), 16);
        int brightness = (r * 299 + g * 587 + b * 114) / 1000;
        this.fontColorCode = brightness > 128 ? "#000000" : "#FFFFFF";
    }

    public Tag(@Nonnull String name, @Nullable String description, @Nonnull String colorCode,
        @Nonnull String fontColorCode) {
        this(name, description);
        this.colorCode = colorCode;
        this.fontColorCode = fontColorCode;
    }
}
