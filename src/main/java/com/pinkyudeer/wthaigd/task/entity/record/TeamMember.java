package com.pinkyudeer.wthaigd.task.entity.record;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pinkyudeer.wthaigd.task.entity.Team;

public class TeamMember extends BaseRecord {

    // 核心关联
    @Nonnull
    public UUID teamId; // 所属团队ID
    @Nonnull
    public UUID playerId; // 玩家ID

    // 成员属性
    @Nonnull
    public LocalDateTime joinTime = LocalDateTime.now(); // 加入时间
    @Nonnull
    public Team.TeamRole role = Team.TeamRole.MEMBER; // 成员角色
    @Nonnull
    public MemberStatus status = MemberStatus.ACTIVE; // 成员状态

    // 贡献统计
    @Nonnull
    public Integer completedTasks = 0; // 完成任务数
    @Nonnull
    public Long contributionPoints = 0L; // 贡献积分
    @Nonnull
    public Duration totalDuration = Duration.ZERO; // 累计贡献时长

    // 操作记录
    @Nullable
    public LocalDateTime lastOperationTime; // 最后操作时间
    @Nullable
    public UUID lastOperatorId; // 最后操作人（用于权限变更记录）

    public TeamMember(@Nonnull UUID teamId, @Nonnull UUID playerId, @Nonnull UUID operatorId) {
        super(operatorId);
        this.teamId = teamId;
        this.playerId = playerId;
    }

    public enum MemberStatus {
        ACTIVE, // 活跃
        SUSPENDED, // 暂停权限
        LEFT // 已离开
    }

    public static class Builder {

        private final UUID teamId;
        private final UUID playerId;
        private final UUID operatorId;

        // 带默认值的可选参数
        private UUID recordId = UUID.randomUUID();
        private LocalDateTime createTime = LocalDateTime.now();
        private LocalDateTime joinTime = LocalDateTime.now();
        private Team.TeamRole role = Team.TeamRole.MEMBER;
        private MemberStatus status = MemberStatus.ACTIVE;
        private Integer completedTasks = 0;
        private Long contributionPoints = 0L;
        private Duration totalDuration = Duration.ZERO;
        private LocalDateTime lastOperationTime;
        private UUID lastOperatorId;

        public Builder(@Nonnull UUID teamId, @Nonnull UUID playerId, @Nonnull UUID operatorId) {
            this.teamId = teamId;
            this.playerId = playerId;
            this.operatorId = operatorId;
        }

        public Builder recordId(UUID val) {
            recordId = val;
            return this;
        }

        public Builder createTime(LocalDateTime val) {
            createTime = val;
            return this;
        }

        public Builder joinTime(LocalDateTime val) {
            joinTime = val;
            return this;
        }

        public Builder role(Team.TeamRole val) {
            role = val;
            return this;
        }

        public Builder status(MemberStatus val) {
            status = val;
            return this;
        }

        public Builder completedTasks(Integer val) {
            completedTasks = val;
            return this;
        }

        public Builder contributionPoints(Long val) {
            contributionPoints = val;
            return this;
        }

        public Builder totalDuration(Duration val) {
            totalDuration = val;
            return this;
        }

        public Builder lastOperationTime(LocalDateTime val) {
            lastOperationTime = val;
            return this;
        }

        public Builder lastOperatorId(UUID val) {
            lastOperatorId = val;
            return this;
        }

        public TeamMember build() {
            TeamMember member = new TeamMember(this.teamId, this.playerId, this.operatorId);
            member.recordId = this.recordId;
            member.createTime = this.createTime;
            member.teamId = this.teamId;
            member.playerId = this.playerId;
            member.joinTime = this.joinTime;
            member.role = this.role;
            member.status = this.status;
            member.completedTasks = this.completedTasks;
            member.contributionPoints = this.contributionPoints;
            member.totalDuration = this.totalDuration;
            member.lastOperationTime = this.lastOperationTime;
            member.lastOperatorId = this.lastOperatorId;
            return member;
        }
    }
}
