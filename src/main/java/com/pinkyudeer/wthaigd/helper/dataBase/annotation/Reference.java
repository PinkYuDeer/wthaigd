package com.pinkyudeer.wthaigd.helper.dataBase.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Reference {

    // 关联的实体
    Class<?> entity();

    // 关联的字段名
    String fieldName() default "id";
}
