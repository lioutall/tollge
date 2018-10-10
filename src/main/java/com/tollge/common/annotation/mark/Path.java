package com.tollge.common.annotation.mark;

import com.tollge.common.annotation.Method;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Path {
    String value() default "";

    Method method() default Method.ROUTE;

    String contentType() default "application/json";
}
