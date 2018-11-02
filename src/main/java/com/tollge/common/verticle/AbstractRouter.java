package com.tollge.common.verticle;

import com.esotericsoftware.reflectasm.MethodAccess;
import com.google.common.collect.Maps;
import com.tollge.common.ResultFormat;
import com.tollge.common.StatusCodeMsg;
import com.tollge.common.UFailureHandler;
import com.tollge.common.annotation.mark.Path;
import com.tollge.common.auth.Subject;
import com.tollge.common.util.Const;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Map;

@Slf4j
public abstract class AbstractRouter {

    public Map<Path, Handler<RoutingContext>> getMap() {
        return map;
    }

    private Map<Path, Handler<RoutingContext>> map = Maps.newHashMap();

    public AbstractRouter() {
        MethodAccess access = MethodAccess.get(this.getClass());
        Method[] ms = this.getClass().getMethods();
        for (Method method : ms) {
            Path p = method.getAnnotation(Path.class);
            if (p == null) {
                continue;
            }
            int methodIndex = access.getIndex(method.getName());
            map.put(p, rct -> access.invoke(this, methodIndex, rct));
        }
    }

    /**
     * 调用biz层
     *
     * @param biz []
     * @param rct []
     */
    protected void sendBiz(String biz, RoutingContext rct) {
        JsonObject jo = rct.getBody() == null || rct.getBody().length() == 0 ? new JsonObject() : rct.getBodyAsJson();
        sendBiz(biz, rct, jo, getAsyncResultHandler(rct));
    }

    private <T> void sendBiz(String biz, RoutingContext rct, JsonObject jo, Handler<AsyncResult<Message<T>>> replyHandler) {
        MultiMap params = rct.request().params();
        if (params != null && !params.isEmpty()) {
            params.forEach(e -> jo.put(e.getKey(), e.getValue()));
        }

        sendBiz(biz, jo, rct, new DeliveryOptions(), replyHandler);
    }

    private <T> void sendBiz(String biz, JsonObject jo, RoutingContext rct, DeliveryOptions deliveryOptions, Handler<AsyncResult<Message<T>>> replyHandler) {
        rct.vertx().eventBus().<T>send(biz, jo, deliveryOptions, replyHandler);
    }

    protected void sendBizWithUser(String biz, RoutingContext rct) {
        JsonObject jo = rct.getBody() == null || rct.getBody().length() == 0 ? new JsonObject() : rct.getBodyAsJson();
        JsonObject currentUser = Subject.getCurrentSubject(rct).getPrincipal();
        jo.put(Const.CURRENT_USER, currentUser);
        jo.put(Const.CURRENT_USER_ID, currentUser.getInteger(Const.ID));
        sendBiz(biz, rct, jo, getAsyncResultHandler(rct));
    }

    private Handler<AsyncResult<Message<Object>>> getAsyncResultHandler(RoutingContext rct) {
        return reply -> {
            if (reply.succeeded()) {
                Object result = reply.result().body();
                // 是分页情况
                if (result instanceof JsonObject && ((JsonObject) result).containsKey(Const.TOLLGE_PAGE_COUNT)) {
                    String pageCount = String.valueOf(((JsonObject) result).getInteger(Const.TOLLGE_PAGE_COUNT));
                    rct.response().putHeader(Const.TOLLGE_PAGE_COUNT, pageCount)
                            .end(ResultFormat.format(StatusCodeMsg.C200, ((JsonObject) result).getJsonArray(Const.TOLLGE_PAGE_DATA)));
                } else {
                    if (rct.response().closed()) {
                        log.error("response is closed, reply:{}", Json.encode(reply.result()));
                    } else {
                        rct.response().end(ResultFormat.format(StatusCodeMsg.C200, reply.result().body()));
                    }
                }
            } else {
                rct.response().end(UFailureHandler.commonFailure(reply.cause()));
            }
        };
    }

    /**
     * 调用biz层
     *
     * @param biz []
     * @param rct []
     * @param replyHandler []
     * @param <T> []
     */
    protected <T> void sendBiz(String biz, RoutingContext rct, Handler<AsyncResult<Message<T>>> replyHandler) {
        JsonObject jo = rct.getBody() == null || rct.getBody().length() == 0 ? new JsonObject() : rct.getBodyAsJson();
        sendBiz(biz, rct, jo, replyHandler);
    }

    protected <T> void sendBizWithUser(String biz, RoutingContext rct, Handler<AsyncResult<Message<T>>> replyHandler) {
        JsonObject jo = rct.getBody() == null || rct.getBody().length() == 0 ? new JsonObject() : rct.getBodyAsJson();
        JsonObject currentUser = Subject.getCurrentSubject(rct).getPrincipal();
        jo.put(Const.CURRENT_USER, currentUser);
        jo.put(Const.CURRENT_USER_ID, currentUser.getInteger(Const.ID));
        sendBiz(biz, rct, jo, replyHandler);
    }
}
