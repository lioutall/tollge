package io.tollge.common.simple;

import io.tollge.common.StatusCodeMsg;
import io.tollge.common.UFailureHandler;
import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.web.RoutingContext;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 简化代码
 *
 * @author toyer
 */
@Slf4j
@NoArgsConstructor
public class SucceedHandle {

    public static <T> void handle(Message msg, AsyncResult<Message<T>> event, MyConsumer bi) {
        if (event.succeeded()) {
            bi.accept();
        } else {
            String address = event.result() == null ? "unknown" : event.result().address();
            log.error("==>[{}] failed", address, event.cause());
            msg.fail(StatusCodeMsg.C500.getCode(), event.cause().getMessage());
        }
    }

    public static <R> void handleFuture(Message<R> msg, AsyncResult<CompositeFuture> event, MyConsumer bi) {
        if (event.succeeded()) {
            bi.accept();
        } else {
            log.error("==>[Future] failed", event.cause());
            msg.fail(StatusCodeMsg.C500.getCode(), event.cause().getMessage());
        }
    }

    public static <T> void handle(RoutingContext rct, AsyncResult<Message<T>> event, MyConsumer bi) {
        if (event.succeeded()) {
            bi.accept();
        } else {
            if (event.result() != null) {
                log.error("==>[{}] failed", event.result().address(), event.cause());
            } else {
                log.error("==>[biz://?] failed", event.cause());
            }
            rct.response().end(UFailureHandler.commonFailure(event.cause()));
        }
    }

}
