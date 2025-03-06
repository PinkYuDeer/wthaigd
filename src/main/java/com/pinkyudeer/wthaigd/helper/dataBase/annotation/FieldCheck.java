package com.pinkyudeer.wthaigd.helper.dataBase.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FieldCheck {

    // 数据类型
    Class<?> dataType();

    // 非特定值
    String notValue() default ""; // 默认无非特定值

    // 最小值和最大值
    String min() default ""; // 默认无最小值

    String max() default ""; // 默认无最大值

    // 数据类型校验类型
    Type type(); // 默认校验字段是否为空

    // GLOB
    String glob() default ""; // 默认无GLOB

    // 不同的校验类型
    enum Type {
        NOT_VALUE, // 非特定值
        MIN, // 最小值
        MAX, // 最大值
        RANGE, // 范围
        GLOB, // GLOB子句
        LENGTH, // 字符串长度（用范围）
        UUID, // UUID（36位长度校验）
        ENUM // 枚举值
    }
}
