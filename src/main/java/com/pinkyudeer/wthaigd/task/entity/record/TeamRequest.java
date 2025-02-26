package com.pinkyudeer.wthaigd.task.entity.record;

import com.pinkyudeer.wthaigd.annotation.Column;
import com.pinkyudeer.wthaigd.annotation.Reference;
import com.pinkyudeer.wthaigd.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "team_requests")
public class TeamRequest extends BaseRecord {

    // 请求类型
    @Nonnull
    @Column(name = "request_type")
    private RequestType requestType; // JOIN-申请加入，INVITE-邀请加入

    // 核心关联
    @Nonnull
    @Column(name = "team_id")
    @Reference(referenceType = Reference.Type.TASK)
    private UUID teamId; // 目标团队ID
    @Nonnull
    @Column(name = "applicant_id")
    @Reference(referenceType = Reference.Type.PLAYER)
    private UUID applicantId; // 申请人/被邀请人ID
    @Nullable
    @Column(name = "inviter_id")
    @Reference(referenceType = Reference.Type.PLAYER)
    private UUID inviterId; // 邀请人ID（仅INVITE类型）

    // 请求内容
    @Nullable
    @Column(name = "reason")
    private String reason; // 申请/邀请理由
    @Nullable
    @Column(name = "expire_time")
    private LocalDateTime expireTime; // 过期时间

    // 处理状态
    @Nonnull
    @Column(name = "status", defaultValue = "'PENDING'")
    private RequestStatus status = RequestStatus.PENDING; // 请求状态
    @Nullable
    @Column(name = "handler_id")
    @Reference(referenceType = Reference.Type.PLAYER)
    private UUID handlerId; // 处理人ID
    @Nullable
    @Column(name = "handle_reason")
    private String handleReason; // 处理理由
    @Nullable
    @Column(name = "handle_time")
    private LocalDateTime handleTime; // 处理时间

    // 来源追踪
    @Nonnull
    @Column(name = "source_type")
    private Notification.SourceType sourceType; // 请求来源

    // 元数据
    @Nullable
    @Column(name = "metadata")
    private String metadata; // 附加信息（如邀请码使用情况）

    public TeamRequest(@Nonnull RequestType requestType, @Nonnull UUID teamId, @Nonnull UUID applicantId,
        @Nonnull Notification.SourceType sourceType, @Nonnull UUID operatorId) {
        super(operatorId);
        this.requestType = requestType;
        this.teamId = teamId;
        this.applicantId = applicantId;
        this.sourceType = sourceType;
    }

    public enum RequestType {
        JOIN, // 玩家主动申请
        INVITE // 团队邀请玩家
    }

    public enum RequestStatus {
        PENDING, // 待处理
        APPROVED, // 已通过
        REJECTED, // 已拒绝
        EXPIRED, // 已过期
        CANCELLED // 已取消
    }

    public static class Builder {

        private final RequestType requestType;
        private final UUID teamId;
        private final UUID applicantId;
        private final Notification.SourceType sourceType;
        private final UUID operatorId;

        // 带默认值的可选参数
        private UUID id;
        private LocalDateTime createTime;
        private UUID inviterId;
        private String reason;
        private LocalDateTime expireTime;
        private RequestStatus status;
        private UUID handlerId;
        private String handleReason;
        private LocalDateTime handleTime;
        private String metadata;

        public Builder(@Nonnull RequestType requestType, @Nonnull UUID teamId, @Nonnull UUID applicantId,
            @Nonnull Notification.SourceType sourceType, @Nonnull UUID operatorId) {
            this.requestType = requestType;
            this.teamId = teamId;
            this.applicantId = applicantId;
            this.sourceType = sourceType;
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

        public Builder inviterId(UUID val) {
            inviterId = val;
            return this;
        }

        public Builder reason(String val) {
            reason = val;
            return this;
        }

        public Builder expireTime(LocalDateTime val) {
            expireTime = val;
            return this;
        }

        public Builder status(RequestStatus val) {
            status = val;
            return this;
        }

        public Builder handlerId(UUID val) {
            handlerId = val;
            return this;
        }

        public Builder handleReason(String val) {
            handleReason = val;
            return this;
        }

        public Builder handleTime(LocalDateTime val) {
            handleTime = val;
            return this;
        }

        public Builder metadata(String val) {
            metadata = val;
            return this;
        }

        public TeamRequest build() {
            TeamRequest request = new TeamRequest(
                this.requestType,
                this.teamId,
                this.applicantId,
                this.sourceType,
                this.operatorId);
            if (this.id != null) request.setId(this.id);
            if (this.createTime != null) request.setCreateTime(this.createTime);
            if (this.inviterId != null) request.setInviterId(this.inviterId);
            if (this.reason != null) request.setReason(this.reason);
            if (this.expireTime != null) request.setExpireTime(this.expireTime);
            if (this.status != null) request.setStatus(this.status);
            if (this.handlerId != null) request.setHandlerId(this.handlerId);
            if (this.handleReason != null) request.setHandleReason(this.handleReason);
            if (this.handleTime != null) request.setHandleTime(this.handleTime);
            if (this.metadata != null) request.setMetadata(this.metadata);
            return request;
        }
    }
}
