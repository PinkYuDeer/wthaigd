package com.pinkyudeer.wthaigd.task.entity.record;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.pinkyudeer.wthaigd.annotation.Column;
import com.pinkyudeer.wthaigd.annotation.FieldCheck;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public abstract class BaseRecord {

    @Nonnull
    @FieldCheck(type = FieldCheck.Type.UUID, dataType = UUID.class)
    @Column(name = "id", isPrimaryKey = true)
    private UUID id;
    @Nonnull
    @Column(name = "create_time", defaultValue = "CURRENT_TIMESTAMP")
    private LocalDateTime createTime;
    @Nonnull
    @FieldCheck(type = FieldCheck.Type.UUID, dataType = UUID.class)
    @Column(name = "operator_id")
    private UUID operatorId;

    public BaseRecord(@Nonnull UUID operatorId) {
        this.id = UUID.randomUUID();
        this.createTime = LocalDateTime.now();
        this.operatorId = operatorId;
    }

    public BaseRecord(@Nonnull LocalDateTime createdAt, @Nonnull UUID operatorId) {
        this.id = UUID.randomUUID();
        this.createTime = createdAt;
        this.operatorId = operatorId;
    }
}
