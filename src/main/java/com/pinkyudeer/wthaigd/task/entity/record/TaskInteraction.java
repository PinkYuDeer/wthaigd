package com.pinkyudeer.wthaigd.task.entity.record;

import java.util.UUID;

import javax.annotation.Nonnull;

public class TaskInteraction extends BaseRecord {

    public TaskInteraction(@Nonnull UUID operatorId) {
        super(operatorId);
    }
}
