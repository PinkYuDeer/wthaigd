package com.pinkyudeer.wthaigd.helper.dataBase.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {

    String name();

    String defaultValue() default "";

    boolean isPrimaryKey() default false;

    boolean isUnique() default false;

    String[] index() default {};
}
