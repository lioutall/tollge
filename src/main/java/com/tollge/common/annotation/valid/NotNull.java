package com.tollge.common.annotation.valid;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Repeatable(NotNulls.class)
public @interface NotNull {
    String key() default "";

    String msg() default "参数不能为空";
}
