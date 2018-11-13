package com.tollge.common.annotation.valid;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface LengthValids {
    LengthValid[] value();
}
