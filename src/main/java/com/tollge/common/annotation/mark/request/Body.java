package com.tollge.common.annotation.mark.request;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Body {
    String value() default "";
}
