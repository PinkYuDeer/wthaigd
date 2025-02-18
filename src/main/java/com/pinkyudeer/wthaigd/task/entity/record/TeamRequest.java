package com.pinkyudeer.wthaigd.task.entity.record;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TeamRequest extends BaseRecord {

    // 请求类型
    @Nonnull
    public RequestType requestType; // JOIN-申请加入，INVITE-邀请加入

    // 核心关联
    @Nonnull
    public UUID teamId; // 目标团队ID
    @Nonnull
    public UUID applicantId; // 申请人/被邀请人ID
    @Nullable
    public UUID inviterId; // 邀请人ID（仅INVITE类型）

    // 请求内容
    @Nullable
    public String reason; // 申请/邀请理由
    @Nullable
    public LocalDateTime expireTime; // 过期时间

    // 处理状态
    @Nonnull
    public RequestStatus status = RequestStatus.PENDING; // 请求状态
    @Nullable
    public UUID handlerId; // 处理人ID
    @Nullable
    public String handleReason; // 处理理由
    @Nullable
    public LocalDateTime handleTime; // 处理时间

    // 来源追踪
    @Nonnull
    public Notification.SourceType sourceType; // 请求来源

    // 元数据
    @Nullable
    public String metadata; // 附加信息（如邀请码使用情况）

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
        private UUID recordId = UUID.randomUUID();
        private LocalDateTime createTime = LocalDateTime.now();
        private UUID inviterId;
        private String reason;
        private LocalDateTime expireTime;
        private RequestStatus status = RequestStatus.PENDING;
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

        public Builder recordId(UUID val) {
            recordId = val;
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
            request.recordId = this.recordId;
            request.createTime = this.createTime;

            request.requestType = this.requestType;
            request.teamId = this.teamId;
            request.applicantId = this.applicantId;
            request.inviterId = this.inviterId;
            request.reason = this.reason;
            request.expireTime = this.expireTime;
            request.status = this.status;
            request.handlerId = this.handlerId;
            request.handleReason = this.handleReason;
            request.handleTime = this.handleTime;
            request.sourceType = this.sourceType;
            request.metadata = this.metadata;
            return request;
        }
    }
}
