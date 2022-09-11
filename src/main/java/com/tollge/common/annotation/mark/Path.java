package com.tollge.common.annotation.mark;

import com.tollge.common.annotation.Method;
import com.tollge.common.util.Const;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Path {
    String value() default "";

    Method method() default Method.ROUTE;

    String contentType() default Const.DEFAULT_CONTENT_TYPE;

    String description() default "";
}
