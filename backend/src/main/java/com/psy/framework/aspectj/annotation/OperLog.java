package com.psy.framework.aspectj.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a management operation that must be written to the audit log.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperLog {

    int OTHER = 0;
    int INSERT = 1;
    int UPDATE = 2;
    int DELETE = 3;

    String title();

    int businessType() default OTHER;
}
