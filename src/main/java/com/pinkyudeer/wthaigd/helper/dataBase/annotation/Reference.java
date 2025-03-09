package com.pinkyudeer.wthaigd.helper.dataBase.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Reference {

    Type referenceType();

    // 此处的referenceType小写后应该与数据库表名一致
    enum Type {
        PLAYER,
        TEAM,
        TASK,
        TAG
    }
}
