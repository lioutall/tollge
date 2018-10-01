package io.tollge.common.annotation.mark;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Biz {
    String value() default "";

    boolean worker() default false;

    int instances() default -1;
}
