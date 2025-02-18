package com.pinkyudeer.wthaigd.task.entity.record;

import java.util.UUID;

import javax.annotation.Nonnull;

public class PlayerInteraction extends BaseRecord {

    public PlayerInteraction(@Nonnull UUID operatorId) {
        super(operatorId);
    }
}
