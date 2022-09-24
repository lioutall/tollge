package com.tollge.common.annotation.mark.request;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface CookieParam {
    String value() default "";

    boolean required() default false;

    int maxLength() default -1;

    int minLength() default -1;

    String regex() default "";

    String description() default "";
}
