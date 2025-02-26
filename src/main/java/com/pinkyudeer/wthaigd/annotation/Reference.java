package com.pinkyudeer.wthaigd.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Reference {

    Type referenceType();

    enum Type {
        PLAYER,
        TEAM,
        TASK,
        TAG,
        PLAYER_INTERACTION,
        TASK_INTERACTION,
        NOTIFICATION,
        TEAM_REQUEST,
        TEAM_MEMBER,
        TASK_HISTORY
    }
}
