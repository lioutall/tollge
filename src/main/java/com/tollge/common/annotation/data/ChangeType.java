package com.tollge.common.annotation.data;

import com.tollge.common.annotation.Type;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Repeatable(ChangeTypes.class)
public @interface ChangeType {
    Type from();

    Type to();

    String key();
}
