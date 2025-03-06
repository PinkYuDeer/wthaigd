package com.pinkyudeer.wthaigd.helper.dataBase.annotation;

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
        TAG
    }
}
