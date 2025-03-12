package com.pinkyudeer.wthaigd.task.entity;

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
@Table(name = "teams")
public class Team {

    // 基础信息
    @Nonnull
    @FieldCheck(type = FieldCheck.Type.UUID, dataType = UUID.class)
    @Column(name = "id", isPrimaryKey = true)
    private UUID id = UUID.randomUUID(); // 团队ID
    @Nonnull
    @Column(name = "name")
    private String name; // 团队名称
    @Nullable
    @Column(name = "description")
    private String description; // 团队描述
    @Nonnull
    @Column(name = "team_code")
    private String teamCode; // 团队加入码

    // 统计信息
    @Nonnull
    @FieldCheck(type = FieldCheck.Type.UUID, dataType = UUID.class)
    @Column(name = "owner_id")
    @Reference(entity = Player.class)
    private UUID ownerId; // 拥有者ID
    @Nonnull
    @Column(name = "total_members", defaultValue = "0")
    private Integer totalMembers = 0; // 总成员数
    @Nonnull
    @Column(name = "total_tasks", defaultValue = "0")
    private Integer totalTasks = 0; // 总任务数
    @Nonnull
    @Column(name = "total_tags", defaultValue = "0")
    private Integer totalTags = 0; // 总标签数
    @Nonnull
    @Column(name = "default_task_visibility", defaultValue = "'TEAM'")
    private Task.PrivacyLevel defaultTaskVisibility = Task.PrivacyLevel.TEAM; // 默认任务可见性

    // 主动操作任务统计
    @Nonnull
    @Column(name = "task_done", defaultValue = "0")
    private Integer taskDone = 0; // 完成任务数
    @Nonnull
    @Column(name = "task_get", defaultValue = "0")
    private Integer taskGet = 0; // 领取任务数
    @Nonnull
    @Column(name = "task_followed", defaultValue = "0")
    private Integer taskFollowed = 0; // 关注任务数
    @Nonnull
    @Column(name = "task_liked", defaultValue = "0")
    private Integer taskLiked = 0; // 点赞任务数
    @Nonnull
    @Column(name = "task_commented", defaultValue = "0")
    private Integer taskCommented = 0; // 评论任务数

    // 被操作任务统计
    @Nonnull
    @Column(name = "task_be_done", defaultValue = "0")
    private Integer taskBeDone = 0; // 任务被完成数
    @Nonnull
    @Column(name = "task_be_get", defaultValue = "0")
    private Integer taskBeGet = 0; // 任务被领取数
    @Nonnull
    @Column(name = "task_be_followed", defaultValue = "0")
    private Integer taskBeFollowed = 0; // 任务被关注数
    @Nonnull
    @Column(name = "task_be_liked", defaultValue = "0")
    private Integer taskBeLiked = 0; // 任务被点赞数
    @Nonnull
    @Column(name = "task_be_commented", defaultValue = "0")
    private Integer taskBeCommented = 0; // 任务被评论数

    // 奖励机制
    @Nonnull
    @Column(name = "reward_points", defaultValue = "0")
    private Long rewardPoints = 0L; // 团队积分
    @Nonnull
    @Column(name = "edit_points", defaultValue = "0")
    private Long editPoints = 0L; // 编辑积分
    @Nonnull
    @Column(name = "work_points", defaultValue = "0")
    private Long workPoints = 0L; // 工作积分
    @Nonnull
    @Column(name = "level", defaultValue = "0")
    private Integer level = 0; // 团队等级

    // 时间信息
    @Nonnull
    @Column(name = "update_time", defaultValue = "CURRENT_TIMESTAMP")
    private LocalDateTime updateTime = LocalDateTime.now(); // 更新时间
    @Nullable
    @Column(name = "disband_time")
    private LocalDateTime disbandTime; // 解散时间

    // 权限设置
    @Nonnull
    @Column(name = "allow_member_create_task", defaultValue = "true")
    public Boolean allowMemberCreateTask = true;
    @Nonnull
    @Column(name = "allow_member_assign_task", defaultValue = "true")
    public Boolean allowMemberAssignTask = true;

    // 请求信息
    @Nonnull
    @Column(name = "join_requests_count", defaultValue = "0")
    private Integer joinRequestsCount = 0; // 加入请求列表
    @Nonnull
    @Column(name = "invitations_count", defaultValue = "0")
    private Integer invitationsCount = 0; // 邀请列表

    public enum TeamRole {
        ADMIN,
        MEMBER,
        GUEST
    }

    public Team(@Nonnull String name, @Nonnull UUID ownerId, @Nullable String description) {
        this.name = name;
        this.ownerId = ownerId;
        if ("".equals(description)) {
            this.description = null;
        } else {
            this.description = description;
        }
        this.teamCode = UUID.randomUUID()
            .toString()
            .substring(0, 8) + "_"
            + Long.toHexString(System.currentTimeMillis());
    }
}
