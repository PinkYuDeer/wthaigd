package com.pinkyudeer.wthaigd.entity.task;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pinkyudeer.wthaigd.helper.dataBase.annotation.Column;
import com.pinkyudeer.wthaigd.helper.dataBase.annotation.FieldCheck;
import com.pinkyudeer.wthaigd.helper.dataBase.annotation.Reference;
import com.pinkyudeer.wthaigd.helper.dataBase.annotation.Table;

import lombok.Data;

@Data
@Table(name = "tags")
public class Tag {

    // 基础属性
    @Nonnull
    @FieldCheck(type = FieldCheck.Type.UUID, dataType = UUID.class)
    @Column(name = "id", isPrimaryKey = true)
    private UUID id = UUID.randomUUID(); // 标签唯一标识符
    @Nonnull
    @Column(name = "name", isUnique = true, index = { "idx_tags_name" })
    private String name; // 标签名称
    @Nullable
    @Column(name = "description")
    private String description; // 标签描述
    @Nonnull
    @Column(name = "color_code", defaultValue = "'#FFFFFF'")
    // TODO:检查项目中所有的颜色字段并添加校验
    private String colorCode = "#FFFFFF"; // 标签颜色
    @Nonnull
    @Column(name = "font_color_code", defaultValue = "'#000000'")
    private String fontColorCode = "#000000"; // 标签字体颜色
    @Nonnull
    @Column(name = "is_default", defaultValue = "false")
    private Boolean isDefault = false; // 是否为默认标签

    // 关联统计
    @Nonnull
    @Column(name = "linked_task_count", defaultValue = "0")
    private transient Integer linkedTaskCount = 0; // 关联任务数
    @Nonnull
    @Column(name = "linked_team_count", defaultValue = "0")
    private transient Integer linkedTeamCount = 0; // 关联团队数
    @Nonnull
    @Column(name = "linked_player_count", defaultValue = "0")
    private transient Integer linkedPlayerCount = 0; // 关联玩家数

    // 权限控制
    @Nonnull
    @Column(name = "scope", defaultValue = "'PUBLIC'")
    private TagScope scope = TagScope.PUBLIC; // 标签可见范围
    @Nullable
    @Column(name = "owner_id")
    @Reference(entity = Player.class)
    private UUID ownerId; // 私有标签拥有者

    // 时间戳
    @Nonnull
    @Column(name = "create_time", defaultValue = "CURRENT_TIMESTAMP")
    private LocalDateTime createTime = LocalDateTime.now(); // 创建时间
    @Nonnull
    @Column(name = "update_time", defaultValue = "CURRENT_TIMESTAMP")
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
