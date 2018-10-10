package com.tollge.common.annotation.valid;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Repeatable(RegexValids.class)
public @interface RegexValid {
    String key();

    String regex();

    String msg() default "参数不符合要求";
}
