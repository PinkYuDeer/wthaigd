package com.pinkyudeer.wthaigd.task.entity.record;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pinkyudeer.wthaigd.task.entity.Team;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TeamMember extends BaseRecord {

    // 核心关联
    @Nonnull
    private UUID teamId; // 所属团队ID
    @Nonnull
    private UUID playerId; // 玩家ID

    // 成员属性
    @Nonnull
    private LocalDateTime joinTime = LocalDateTime.now(); // 加入时间
    @Nonnull
    private Team.TeamRole role = Team.TeamRole.MEMBER; // 成员角色
    @Nonnull
    private MemberStatus status = MemberStatus.ACTIVE; // 成员状态

    // 贡献统计
    @Nonnull
    private Integer completedTasks = 0; // 完成任务数
    @Nonnull
    private Long contributionPoints = 0L; // 贡献积分
    @Nonnull
    private Duration totalDuration = Duration.ZERO; // 累计贡献时长

    // 操作记录
    @Nullable
    private LocalDateTime lastOperationTime; // 最后操作时间
    @Nullable
    private UUID lastOperatorId; // 最后操作人（用于权限变更记录）

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
        private UUID id;
        private LocalDateTime createTime;
        private LocalDateTime joinTime;
        private Team.TeamRole role;
        private MemberStatus status;
        private Integer completedTasks;
        private Long contributionPoints;
        private Duration totalDuration;
        private LocalDateTime lastOperationTime;
        private UUID lastOperatorId;

        public Builder(@Nonnull UUID teamId, @Nonnull UUID playerId, @Nonnull UUID operatorId) {
            this.teamId = teamId;
            this.playerId = playerId;
            this.operatorId = operatorId;
        }

        public Builder id(UUID val) {
            id = val;
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
            if (this.id != null) member.setId(this.id);
            if (this.createTime != null) member.setCreateTime(this.createTime);
            if (this.joinTime != null) member.setJoinTime(this.joinTime);
            if (this.role != null) member.setRole(this.role);
            if (this.status != null) member.setStatus(this.status);
            if (this.completedTasks != null) member.setCompletedTasks(this.completedTasks);
            if (this.contributionPoints != null) member.setContributionPoints(this.contributionPoints);
            if (this.totalDuration != null) member.setTotalDuration(this.totalDuration);
            if (this.lastOperationTime != null) member.setLastOperationTime(this.lastOperationTime);
            if (this.lastOperatorId != null) member.setLastOperatorId(this.lastOperatorId);
            return member;
        }
    }
}
