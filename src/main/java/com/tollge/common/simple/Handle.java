package com.tollge.common.simple;

import com.tollge.common.StatusCodeMsg;
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
public class Handle {

    public static <T> Handler<AsyncResult<T>> assertSuccess(Message msg, Handler<T> resultHandler) {
        return ar -> {
            if (ar.succeeded()) {
                T result = ar.result();
                try {
                    resultHandler.handle(result);
                } catch (Exception e) {
                    log.error("assertSuccess handle failed", e);
                    msg.fail(StatusCodeMsg.C500.getCode(), e.getMessage());
                }
            } else {
                msg.fail(StatusCodeMsg.C500.getCode(), ar.cause().getMessage());
            }
        };
    }

    public static <T> Handler<AsyncResult<T>> assertSuccess(RoutingContext rct, Handler<T> resultHandler) {
        return ar -> {
            if (ar.succeeded()) {
                T result = ar.result();
                try {
                    resultHandler.handle(result);
                } catch (Exception e) {
                    log.error("assertSuccess handle failed", e);
                    rct.response().end(UFailureHandler.commonFailure(e));
                }
            } else {
                rct.response().end(UFailureHandler.commonFailure(ar.cause()));
            }
        };
    }

    public static <T> Handler<AsyncResult<T>> fromHandler(Promise<T> reply) {
        return (r) -> {
            if (r.succeeded()) {
                reply.complete(r.result());
            } else {
                log.error("fromHandler error", r.cause());
                reply.fail(r.cause().getMessage());
            }
        };
    }
}
