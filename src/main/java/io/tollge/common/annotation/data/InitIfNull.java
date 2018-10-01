package io.tollge.common.annotation.data;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Repeatable(InitIfNulls.class)
public @interface InitIfNull {
    String key();

    String value();
}
