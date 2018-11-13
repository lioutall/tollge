package com.tollge.common.annotation.valid;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Repeatable(LengthValids.class)
public @interface LengthValid {
    String key();

    int min() default -1;

    int max() default -1;

    String msg() default "参数长度不对";
}
