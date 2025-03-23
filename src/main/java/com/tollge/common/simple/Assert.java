package com.tollge.common.simple;

import com.tollge.common.StatusCodeMsg;
import com.tollge.common.TollgeException;
import com.tollge.common.UFailureHandler;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.web.RoutingContext;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
public class Assert {
    
    public static void check(boolean condition, String msg) {
        if (!condition) {
            log.warn(msg);
            throw new IllegalArgumentException(msg);
        }
    }
    
    public static void check(boolean condition, String msg, Object... args) {
        if (!condition) {
            log.warn(msg, args);
            throw new IllegalArgumentException(msg);
        }
    }
    
    public static void check(boolean condition, String msg, Throwable e) {
        if (!condition) {
            log.error(msg, e);
            throw new TollgeException(msg, e);
        }
    }
    
}
