package com.tollge.common.verticle;

import com.esotericsoftware.reflectasm.MethodAccess;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.tollge.common.ResultFormat;
import com.tollge.common.StatusCodeMsg;
import com.tollge.common.TollgeException;
import com.tollge.common.UFailureHandler;
import com.tollge.common.annotation.mark.Path;
import com.tollge.common.annotation.mark.request.*;
import com.tollge.common.auth.Subject;
import com.tollge.common.util.Const;
import com.tollge.common.util.MyVertx;
import io.vertx.core.*;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
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
            map.put(p, rct -> {
                Object[] parameters = null;
                try {
                    // 查看method的参数数量
                    if (method.getParameterCount() != 0) {
                        // 找到rct
                        Class<?>[] parameterTypes = method.getParameterTypes();
                        if (Arrays.stream(parameterTypes).noneMatch(o -> o.isAssignableFrom(RoutingContext.class))) {
                            throw new TollgeException("cant find RoutingContext in path:" + p.value());
                        }

                        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
                        parameters = new Object[parameterTypes.length];
                        for (int i = 0; i < parameterTypes.length; i++) {
                            if (parameterTypes[i].isAssignableFrom(RoutingContext.class)) {
                                parameters[i] = rct;
                            } else {
                                Annotation[] indexAnnotations = parameterAnnotations[i];
                                if (indexAnnotations.length > 1) {
                                    throw new TollgeException("path[" + p.value() + "].method[" + method.getName()
                                            + "].parameter[" + parameters[i].getClass().getName() + "] annotation more than 1");
                                } else if (indexAnnotations.length < 1) {
                                    throw new TollgeException("path[" + p.value() + "].method[" + method.getName()
                                            + "].parameter[" + parameters[i].getClass().getName() + "] annotation is null");
                                }

                                HttpServerRequest request = rct.request();
                                if (indexAnnotations[0].annotationType().isAssignableFrom(CookieParam.class)) {
                                    CookieParam cookieParam = (CookieParam) indexAnnotations[0];
                                    parameters[i] = request.getCookie(cookieParam.value());
                                    Preconditions.checkArgument(cookieParam.required() && parameters[i] != null, cookieParam.value() + " is required");
                                    if (parameters[i] != null) {
                                        parameters[i] = request.getCookie(cookieParam.value()).getValue();
                                    }
                                    checkMinLength((String) parameters[i], cookieParam.value(), cookieParam.minLength());
                                    checkMaxLength((String) parameters[i], cookieParam.value(), cookieParam.maxLength());
                                    checkRegex((String) parameters[i], cookieParam.value(), cookieParam.regex());
                                } else if (indexAnnotations[0].annotationType().isAssignableFrom(FormParam.class)) {
                                    FormParam formParam = (FormParam) indexAnnotations[0];
                                    parameters[i] = request.getFormAttribute(formParam.value());
                                    Preconditions.checkArgument(formParam.required() && parameters[i] != null, formParam.value() + " is required");
                                    checkMinLength((String) parameters[i], formParam.value(), formParam.minLength());
                                    checkMaxLength((String) parameters[i], formParam.value(), formParam.maxLength());
                                    checkRegex((String) parameters[i], formParam.value(), formParam.regex());
                                } else if (indexAnnotations[0].annotationType().isAssignableFrom(HeaderParam.class)) {
                                    HeaderParam headerParam = (HeaderParam) indexAnnotations[0];
                                    parameters[i] = request.getHeader(headerParam.value());
                                    Preconditions.checkArgument(headerParam.required() && parameters[i] != null, headerParam.value() + " is required");
                                    checkMinLength((String) parameters[i], headerParam.value(), headerParam.minLength());
                                    checkMaxLength((String) parameters[i], headerParam.value(), headerParam.maxLength());
                                    checkRegex((String) parameters[i], headerParam.value(), headerParam.regex());
                                } else if (indexAnnotations[0].annotationType().isAssignableFrom(PathParam.class)) {
                                    PathParam pathParam = (PathParam) indexAnnotations[0];
                                    parameters[i] = request.getParam(((PathParam) indexAnnotations[0]).value());
                                    Preconditions.checkArgument(pathParam.required() && parameters[i] != null, pathParam.value() + " is required");
                                    checkMinLength((String) parameters[i], pathParam.value(), pathParam.minLength());
                                    checkMaxLength((String) parameters[i], pathParam.value(), pathParam.maxLength());
                                    checkRegex((String) parameters[i], pathParam.value(), pathParam.regex());
                                } else if (indexAnnotations[0].annotationType().isAssignableFrom(QueryParam.class)) {
                                    QueryParam param = (QueryParam) indexAnnotations[0];
                                    parameters[i] = request.getParam(param.value());
                                    Preconditions.checkArgument(param.required() && parameters[i] != null, param.value() + " is required");
                                    checkMinLength((String) parameters[i], param.value(), param.minLength());
                                    checkMaxLength((String) parameters[i], param.value(), param.maxLength());
                                    checkRegex((String) parameters[i], param.value(), param.regex());
                                } else if (indexAnnotations[0].annotationType().isAssignableFrom(Body.class)) {
                                    if (rct.getBody() == null || rct.getBody().length() == 0) {
                                        parameters[i] = Json.decodeValue(new JsonObject().toBuffer(), parameterTypes[i]);
                                    } else {
                                        parameters[i] = Json.decodeValue(rct.getBody(), parameterTypes[i]);
                                    }
                                    //TODO 校验
                                } else {
                                    throw new TollgeException("path[" + p.value() + "].method[" + method.getName()
                                            + "].parameter[" + parameters[i].getClass().getName() + "] annotation not exist");
                                }
                            }
                        }

                    }
                    // 处理
                    Future<Object> reply = (Future<Object>) access.invoke(this, methodIndex, parameters);
                    reply.onComplete(res -> {
                        if (res.succeeded()) {
                            Object result = res.result();
                            // 是分页情况
                            if (result instanceof JsonObject && ((JsonObject) result).containsKey(Const.TOLLGE_PAGE_COUNT)) {
                                String pageCount = String.valueOf(((JsonObject) result).getInteger(Const.TOLLGE_PAGE_COUNT));
                                rct.response().putHeader(Const.TOLLGE_PAGE_COUNT, pageCount)
                                        .end(ResultFormat.format(StatusCodeMsg.C200, ((JsonObject) result).getJsonArray(Const.TOLLGE_PAGE_DATA)));
                            } else {
                                if (rct.response().closed()) {
                                    log.error("response is closed, reply:{}", Json.encode(res));
                                } else {
                                    rct.response().end(ResultFormat.format(StatusCodeMsg.C200, res.result()));
                                }
                            }
                        } else {
                            rct.response().end(UFailureHandler.commonFailure(res.cause()));
                        }
                    });


                } catch (Exception e) {
                    rct.response().end(UFailureHandler.commonFailure(e));
                }
            });
        }
    }

    private void checkRegex(String parameter, String name, String regex) {
        if (!"".equals(regex)) {
            Preconditions.checkArgument(parameter!=null, name + "格式不正确");
            Preconditions.checkArgument(parameter.matches(regex), name + "格式不正确");
        }
    }

    private void checkMaxLength(String parameter, String name, int maxLength) {
        if (maxLength > 0) {
            if (parameter != null) {
                Preconditions.checkArgument(parameter.length() > maxLength, name + " length should less than " + maxLength);
            }
        }
    }

    private void checkMinLength(String parameter, String name, int minLength) {
        if (minLength > 0) {
            Preconditions.checkArgument(parameter!=null, name + " length less than " + minLength);
            Preconditions.checkArgument(parameter.length() < minLength, name + " length should larger than " + minLength);
        }
    }

    /**
     * 调用biz层
     */
    protected <T> AsyncResult<T> sendBiz(String biz) {
        return Future.future(reply -> sendBiz(biz, getReplyHandler(reply)));
    }

    protected <T> AsyncResult<T> sendBiz(String biz, Object o) {
        return Future.future(reply -> sendBiz(biz, o, getReplyHandler(reply)));
    }

    protected <T> AsyncResult<T> sendBiz(String biz, Handler<AsyncResult<Message<T>>> replyHandler) {
        return Future.future(reply -> sendBiz(biz, null, new DeliveryOptions(), replyHandler));
    }

    protected <T> AsyncResult<T> sendBiz(String biz, Object o, Handler<AsyncResult<Message<T>>> replyHandler) {
        return Future.future(reply -> sendBiz(biz, o, new DeliveryOptions(), replyHandler));
    }

    /**
     * 以下-----------     增加用户属性
     */
    protected <T> AsyncResult<T> sendBizWithUser(JsonObject currentUser, String biz) {
        return Future.future(reply -> sendBizWithUser(currentUser, biz, getReplyHandler(reply)));
    }

    protected <T> AsyncResult<T> sendBizWithUser(JsonObject currentUser, String biz, Object o) {
        return Future.future(reply -> sendBizWithUser(currentUser, biz, o, getReplyHandler(reply)));
    }

    protected <T> AsyncResult<T> sendBizWithUser(JsonObject currentUser, String biz, Handler<AsyncResult<Message<T>>> replyHandler) {
        DeliveryOptions deliveryOptions = new DeliveryOptions();
        deliveryOptions.addHeader(Const.CURRENT_USER, currentUser.encode());
        return Future.future(reply -> sendBiz(biz, null, deliveryOptions, replyHandler));
    }

    protected <T> AsyncResult<T> sendBizWithUser(JsonObject currentUser, String biz, Object o, Handler<AsyncResult<Message<T>>> replyHandler) {
        DeliveryOptions deliveryOptions = new DeliveryOptions();
        deliveryOptions.addHeader(Const.CURRENT_USER, currentUser.encode());
        return Future.future(reply -> sendBiz(biz, o, deliveryOptions, replyHandler));
    }

    protected <T> AsyncResult<T> sendBizWithUser(RoutingContext rct, String biz) {
        JsonObject currentUser = Subject.getCurrentSubject(rct).getPrincipal();
        return Future.future(reply -> sendBizWithUser(currentUser, biz, getReplyHandler(reply)));
    }

    protected <T> AsyncResult<T> sendBizWithUser(RoutingContext rct, String biz, Object o) {
        JsonObject currentUser = Subject.getCurrentSubject(rct).getPrincipal();
        return Future.future(reply -> sendBizWithUser(currentUser, biz, o, getReplyHandler(reply)));
    }

    protected <T> AsyncResult<T> sendBizWithUser(RoutingContext rct, String biz, Handler<AsyncResult<Message<T>>> replyHandler) {
        JsonObject currentUser = Subject.getCurrentSubject(rct).getPrincipal();
        DeliveryOptions deliveryOptions = new DeliveryOptions();
        deliveryOptions.addHeader(Const.CURRENT_USER, currentUser.encode());
        return Future.future(reply -> sendBiz(biz, null, deliveryOptions, replyHandler));
    }

    protected <T> AsyncResult<T> sendBizWithUser(RoutingContext rct, String biz, Object o, Handler<AsyncResult<Message<T>>> replyHandler) {
        JsonObject currentUser = Subject.getCurrentSubject(rct).getPrincipal();
        DeliveryOptions deliveryOptions = new DeliveryOptions();
        deliveryOptions.addHeader(Const.CURRENT_USER, currentUser.encode());
        return Future.future(reply -> sendBiz(biz, o, deliveryOptions, replyHandler));
    }

    /**
     * 底层调用
     */
    private <T> AsyncResult sendBiz(String biz, Object jo, DeliveryOptions deliveryOptions, Handler<AsyncResult<Message<T>>> replyHandler) {
        return Future.future(reply -> MyVertx.vertx().eventBus().<T>request(biz, jo, deliveryOptions, replyHandler));
    }

    private <T> Handler<AsyncResult<Message<T>>> getReplyHandler(Promise<T> reply) {
        return messageReply -> {
            if (messageReply.succeeded()) {
                reply.complete(messageReply.result().body());
            } else {
                reply.fail(messageReply.cause());
            }
        };
    }
}
