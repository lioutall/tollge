package com.tollge.common.annotation.data;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface InitIfNulls {
    InitIfNull[] value();
}
