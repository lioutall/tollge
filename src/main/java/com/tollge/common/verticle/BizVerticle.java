package com.tollge.common.verticle;

import com.esotericsoftware.reflectasm.MethodAccess;
import com.google.common.base.Preconditions;
import com.tollge.common.*;
import com.tollge.common.annotation.data.ChangeType;
import com.tollge.common.annotation.data.ChangeTypes;
import com.tollge.common.annotation.data.InitIfNull;
import com.tollge.common.annotation.data.InitIfNulls;
import com.tollge.common.annotation.mark.Biz;
import com.tollge.common.annotation.mark.Path;
import com.tollge.common.annotation.valid.*;
import com.tollge.common.auth.LoginUser;
import com.tollge.common.util.Const;
import com.tollge.common.util.MyVertx;
import io.netty.util.internal.StringUtil;
import io.vertx.core.*;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static com.tollge.common.util.Const.RETURN_CLASS_TYPE;

/**
 * 业务逻辑抽象类
 * @author tollge
 */
@Slf4j
public class BizVerticle extends AbstractVerticle {

    private final BiFunction<Message<?>, SqlAndParams, Handler<AsyncResult<Message<Object>>>> bizResultHandler = (msg, sqlAndParams) -> res -> {
        if (res.succeeded()) {
            msg.reply(res.result().body());
        } else {
            if (res.cause() instanceof IllegalArgumentException) {
                msg.fail(StatusCodeMsg.C414.getCode(), res.cause().getMessage());
            } else {
                log.error("Biz failed", res.cause());
                msg.fail(StatusCodeMsg.C501.getCode(), res.cause().getMessage());
            }
        }
    };

    @Override
    public void start() {
        MethodAccess access = MethodAccess.get(this.getClass());
        Arrays.stream(this.getClass().getMethods()).forEach(m -> {
            Path pathMark = m.getAnnotation(Path.class);
            if (pathMark == null) {
                return;
            }
            String path = this.getClass().getAnnotation(Biz.class).value() + pathMark.value();
            int methodIndex = access.getIndex(m.getName());
            vertx.eventBus().consumer(path, msg -> {
                try {
                    // 业务前校验和初始化
                    // validateAndInit(m, msg);
                    // 最终调用
                    access.invoke(this, methodIndex, msg);
                } catch (IllegalArgumentException e) {
                    msg.fail(StatusCodeMsg.C414.getCode(), e.getMessage());
                } catch (Exception e) {
                    log.error("check or init error, path:{}", path, e);
                    msg.fail(StatusCodeMsg.C500.getCode(), e.getMessage());
                }
            });
            log.info("服务 {}", path);
        });
    }

    /**
     * 业务前校验和初始化
     * @param m 方法
     * @param msg vertx msg
     */
    private void validateAndInit(Method m, Message<JsonObject> msg) {
        JsonObject body = msg.body();
        boolean hasBody = body != null;
        for (Annotation annotation : m.getDeclaredAnnotations()) {
            // 校验
            if (annotation instanceof NotNull) {
                validate(body, hasBody, (NotNull) annotation);
            } else if (annotation instanceof NotNulls) {
                validate(body, hasBody, (NotNulls) annotation);
            } else if (annotation instanceof LengthValid) {
                validate(body, hasBody, (LengthValid) annotation);
            } else if (annotation instanceof LengthValids) {
                validate(body, hasBody, (LengthValids) annotation);
            } else if (annotation instanceof RegexValid) {
                validate(body, hasBody, (RegexValid) annotation);
            } else if (annotation instanceof RegexValids) {
                validate(body, hasBody, (RegexValids) annotation);
            }

            if (hasBody) {
                try {
                    // 初始化
                    if (annotation instanceof InitIfNulls) {
                        init(body, (InitIfNulls) annotation);
                    } else if (annotation instanceof InitIfNull) {
                        init(body, (InitIfNull) annotation);
                    }
                    // 类型转换
                    else if (annotation instanceof ChangeTypes) {
                        changeType(body, (ChangeTypes) annotation);
                    } else if (annotation instanceof ChangeType) {
                        changeType(body, (ChangeType) annotation);
                    }
                } catch (Exception e) {
                    log.error("[{}]Init data error", msg.address(), e);
                    throw e;
                }
            }
        }
    }

    private void changeType(JsonObject body, ChangeTypes annotation) {
        ChangeTypes toTypes = annotation;
        ChangeType[] ts = toTypes.value();
        for (ChangeType t : ts) {
            changeType(body, t);
        }
    }

    private void changeType(JsonObject body, ChangeType t) {
        if (body.containsKey(t.key())) {
            switch (t.to()) {
                case STRING:
                    switch (t.from()) {
                        case INTEGER:
                        case DOUBLE:
                            body.put(t.key(), body.getValue(t.key()).toString());
                            break;
                        default:
                            break;
                    }
                    break;

                case INTEGER:
                    switch (t.from()) {
                        case STRING:
                            body.put(t.key(), Integer.valueOf(body.getString(t.key())));
                            break;
                        case DOUBLE:
                            body.put(t.key(), body.getDouble(t.key()).intValue());
                            break;
                        default:
                            break;
                    }
                    break;
                case DOUBLE:
                    switch (t.from()) {
                        case STRING:
                            body.put(t.key(), Double.valueOf(body.getString(t.key())));
                            break;
                        case INTEGER:
                            body.put(t.key(), Double.valueOf(body.getInteger(t.key())));
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void init(JsonObject body, InitIfNulls annotation) {
        InitIfNull[] is = annotation.value();
        for (InitIfNull i1 : is) {
            init(body, i1);
        }
    }

    private void init(JsonObject body, InitIfNull i1) {
        if (!body.containsKey(i1.key()) || StringUtil.isNullOrEmpty(body.getString(i1.key()))) {
            body.put(i1.key(), i1.value());
        }
    }

    private void validate(JsonObject body, boolean hasBody, NotNulls ns) {
        for (NotNull n : ns.value()) {
            validate(body, hasBody, n);
        }
    }

    private void validate(JsonObject body, boolean hasBody, NotNull n) {
        Preconditions.checkArgument(hasBody, n.msg());
        Object value = body.getValue(n.key());
        if (value instanceof String) {
            Preconditions.checkArgument(!StringUtil.isNullOrEmpty(body.getString(n.key())), n.msg());
        } else {
            Preconditions.checkArgument(value != null, n.msg());
        }
    }

    private void validate(JsonObject body, boolean hasBody, LengthValids ns) {
        for (LengthValid n : ns.value()) {
            validate(body, hasBody, n);
        }
    }

    private void validate(JsonObject body, boolean hasBody, LengthValid n) {
        Preconditions.checkArgument(hasBody, n.msg());
        Object value = body.getValue(n.key());

        int strLen = -1;
        if (value instanceof String) {
            strLen = body.getString(n.key()).length();
        } else if (value instanceof JsonArray) {
            strLen = body.getJsonArray(n.key()).size();
        }
        if(strLen == -1){
            log.error("请查看是否错误的使用LengthValid with " + n.key());
        } else {
            Preconditions.checkArgument(n.min() == -1 || strLen >= n.min(), n.msg() + "," + n.key() + "的长度是" + strLen);
            Preconditions.checkArgument(n.max() == -1 || strLen <= n.max(), n.msg() + "," + n.key() + "的长度是" + strLen);
        }
    }

    private void validate(JsonObject body, boolean hasBody, RegexValids ns) {
        for (RegexValid n : ns.value()) {
            validate(body, hasBody, n);
        }
    }

    private void validate(JsonObject body, boolean hasBody, RegexValid n) {
        Preconditions.checkArgument(hasBody, n.msg());
        Object value = body.getValue(n.key());
        if (value instanceof String) {
            String v = body.getString(n.key());
            Preconditions.checkArgument(v != null
                    && v.matches(n.regex()), n.msg());
        } else {
            throw new IllegalArgumentException(n.key() + "is not string");
        }
    }

    /**********************
     *  以下是sql方法封装, 便于使用
     * @param msg []
     * @param sqlAndParams []
     */
    protected void one(Message<?> msg, SqlAndParams sqlAndParams) {
        sendDB(AbstractDao.ONE, JsonObject.mapFrom(sqlAndParams), bizResultHandler.apply(msg, sqlAndParams), null);
    }

    protected void one(Message<?> msg, SqlAndParams sqlAndParams, Class<?> cls) {
        sendDB(AbstractDao.ONE, JsonObject.mapFrom(sqlAndParams), bizResultHandler.apply(msg, sqlAndParams), cls);
    }

    protected void one(String sqlId, Message<?> msg, JsonObject append) {
        SqlAndParams sqlAndParams = generateSqlAndParams(sqlId, msg, append);
        sendDB(AbstractDao.ONE, JsonObject.mapFrom(sqlAndParams), bizResultHandler.apply(msg, sqlAndParams), null);
    }

    protected void one(String sqlId, Message<?> msg, JsonObject append, Class<?> cls) {
        SqlAndParams sqlAndParams = generateSqlAndParams(sqlId, msg, append);
        sendDB(AbstractDao.ONE, JsonObject.mapFrom(sqlAndParams), bizResultHandler.apply(msg, sqlAndParams), cls);
    }

    protected void one(String sqlId, Message<?> msg, BaseModel query) {
        one(sqlId, msg, new JsonObject(Json. encode(query)));
    }

    protected void one(String sqlId, Message<?> msg, BaseModel query, Class<?> cls) {
        one(sqlId, msg, new JsonObject(Json. encode(query)), cls);
    }

    protected <T> void one(String sqlId, Message<?> msg, JsonObject append, Handler<AsyncResult<Message<T>>> replyHandler, Class<T> cls) {
        SqlAndParams sqlAndParams = generateSqlAndParams(sqlId, msg, append);
        sendDB(AbstractDao.ONE, JsonObject.mapFrom(sqlAndParams), replyHandler, cls);
    }

    protected <T> void one(String sqlId, Message<?> msg, BaseModel query, Handler<AsyncResult<Message<T>>> replyHandler, Class<T> cls) {
        one(sqlId, msg, new JsonObject(Json. encode(query)), replyHandler, cls);
    }

    protected <T> void one(String sqlId, BaseModel query, Handler<AsyncResult<Message<T>>> replyHandler, Class<T> cls) {
        one(sqlId, new JsonObject(Json. encode(query)), replyHandler, cls);
    }

    protected <T> void one(String sqlId, JsonObject append, Handler<AsyncResult<Message<T>>> replyHandler, Class<T> cls) {
        SqlAndParams sqlAndParams = new SqlAndParams(sqlId);
        if (append != null && !append.isEmpty()) {
            append.forEach(entry -> sqlAndParams.putParam(entry.getKey(), entry.getValue()));
        }
        sendDB(AbstractDao.ONE, JsonObject.mapFrom(sqlAndParams), replyHandler, cls);
    }

    private SqlAndParams generateSqlAndParams(String sqlId, Message<?> msg, JsonObject append) {
        SqlAndParams sqlAndParams = new SqlAndParams(sqlId);
        Object body = msg.body();
        JsonObject o = null;
        if (body instanceof JsonObject) {
            o = (JsonObject)body;
        } else {
            o = JsonObject.mapFrom(body);
        }
        if (o != null && !o.isEmpty()) {
            o.forEach(entry -> sqlAndParams.putParam(entry.getKey(), entry.getValue()));
        }

        if (append != null && !append.isEmpty()) {
            append.forEach(entry -> sqlAndParams.putParam(entry.getKey(), entry.getValue()));
        }
        return sqlAndParams;
    }

    protected void count(Message<?> msg, SqlAndParams sqlAndParams) {
        sendDB(AbstractDao.COUNT, JsonObject.mapFrom(sqlAndParams), bizResultHandler.apply(msg, sqlAndParams), Long.class);
    }

    protected void count(String sqlId, Message<?> msg, JsonObject append) {
        SqlAndParams sqlAndParams = generateSqlAndParams(sqlId, msg, append);
        sendDB(AbstractDao.COUNT, JsonObject.mapFrom(sqlAndParams), bizResultHandler.apply(msg, sqlAndParams), Long.class);
    }

    protected void count(String sqlId, Message<?> msg, BaseModel query) {
        count(sqlId, msg, new JsonObject(Json.encode(query)));
    }

    protected <T> void count(String sqlId, Message<?> msg, JsonObject append, Handler<AsyncResult<Message<T>>> replyHandler) {
        SqlAndParams sqlAndParams = generateSqlAndParams(sqlId, msg, append);
        sendDB(AbstractDao.COUNT, JsonObject.mapFrom(sqlAndParams), replyHandler, Long.class);
    }

    protected <T> void count(String sqlId, Message<?> msg, BaseModel query, Handler<AsyncResult<Message<T>>> replyHandler) {
        count(sqlId, msg, new JsonObject(Json.encode(query)), replyHandler);
    }

    protected <T> void list(Message<?> msg, SqlAndParams sqlAndParams) {
        sendDB(AbstractDao.LIST, JsonObject.mapFrom(sqlAndParams), bizResultHandler.apply(msg, sqlAndParams), null);
    }

    protected <T> void list(Message<?> msg, SqlAndParams sqlAndParams, Class<T> cls) {
        sendDB(AbstractDao.LIST, JsonObject.mapFrom(sqlAndParams), bizResultHandler.apply(msg, sqlAndParams), cls);
    }

  protected <T> Future<Page<T>> page(String sqlId, JsonObject append, Class<T> cls) {
    return Future.future(reply -> {
      SqlAndParams sqlAndParams = new SqlAndParams(sqlId, append.getInteger("pageNum"), append.getInteger("pageSize"));
      for (Map.Entry<String, Object> entry : append) {
        sqlAndParams.putParam(entry.getKey(), entry.getValue());
      }
      sendDB(AbstractDao.PAGE, JsonObject.mapFrom(sqlAndParams), getReplyHandler(reply), cls);});
  }

  protected <T> Future<Page<T>> page(String sqlId, PageRequest request, Class<T> cls) {
    return Future.future(reply -> {
      SqlAndParams sqlAndParams = new SqlAndParams(sqlId, request.getPageNum(), request.getPageSize());
      for (Map.Entry<String, Object> entry : JsonObject.mapFrom(request)) {
        sqlAndParams.putParam(entry.getKey(), entry.getValue());
      }
      sendDB(AbstractDao.PAGE, JsonObject.mapFrom(sqlAndParams), getReplyHandler(reply), cls);});
  }

    protected void list(String sqlId, Message<?> msg, JsonObject append) {
        list(sqlId, msg, append, replyHandler(msg), null);
    }
    protected <T> void list(String sqlId, Message<?> msg, JsonObject append, Class<T> cls) {
        list(sqlId, msg, append, replyHandler(msg), cls);
    }
    private static <T> Handler<AsyncResult<Message<List<T>>>> replyHandler(Message<?> msg) {
        return res -> {
            if (res.succeeded()) {
                msg.reply(res.result().body());
            } else {
                if (res.cause() instanceof IllegalArgumentException) {
                    msg.fail(StatusCodeMsg.C414.getCode(), res.cause().getMessage());
                } else {
                    log.error("Biz failed", res.cause());
                    msg.fail(StatusCodeMsg.C501.getCode(), res.cause().getMessage());
                }
            }
        };
    }
    protected void list(String sqlId, Message<?> msg, BaseModel query) {
        list(sqlId, msg, new JsonObject(Json.encode(query)), replyHandler(msg), null);
    }

    protected <T> void list(String sqlId, Message<?> msg, BaseModel query, Class<T> cls) {
        list(sqlId, msg, new JsonObject(Json.encode(query)), replyHandler(msg), cls);
    }

    protected <T> void list(String sqlId, Message<?> msg, BaseModel query, Handler<AsyncResult<Message<List<T>>>> replyHandler, Class<T> cls) {
        list(sqlId, msg, new JsonObject(Json.encode(query)), replyHandler, cls);
    }

    protected <T> Future<List<T>> list(String sqlId, JsonObject append, Class<T> cls) {
        return Future.future(reply -> {
            SqlAndParams sqlAndParams = new SqlAndParams(sqlId);
            for (Map.Entry<String, Object> entry : append) {
                sqlAndParams.putParam(entry.getKey(), entry.getValue());
            }
            sendDB(AbstractDao.LIST, JsonObject.mapFrom(sqlAndParams), getReplyHandler(reply), cls);});
    }

    protected <T> void list(String sqlId, Message<?> msg, JsonObject append, Handler<AsyncResult<Message<List<T>>>> replyHandler, Class<T> cls) {
        SqlAndParams sqlAndParams = new SqlAndParams(sqlId);
        String daoId = AbstractDao.LIST;
        Object body = msg.body();
        JsonObject o = null;
        if (body instanceof JsonObject) {
            o = (JsonObject)body;
        } else {
            o = JsonObject.mapFrom(body);
        }
        if (o != null && !o.isEmpty()) {
            // 如果是分页
            if (o.containsKey("pageNum") && o.containsKey("pageSize")) {
                sqlAndParams = new SqlAndParams(sqlId, Integer.valueOf(o.getString("pageNum")),
                        Integer.valueOf(o.getString("pageSize")));
                daoId = AbstractDao.PAGE;
                o.remove("pageNum");
                o.remove("pageSize");
            }

            for (Map.Entry<String, Object> entry : o) {
                sqlAndParams.putParam(entry.getKey(), entry.getValue());
            }
        }

        if (append != null && !append.isEmpty()) {
            for (Map.Entry<String, Object> entry : append) {
                sqlAndParams.putParam(entry.getKey(), entry.getValue());
            }
        }

        sendDB(daoId, JsonObject.mapFrom(sqlAndParams), replyHandler, cls);
    }

    protected <T> void operate(String sqlId, Message<?> msg, JsonObject append, Handler<AsyncResult<Message<OperationResult<T>>>> replyHandler, Class<T> cls) {
        SqlAndParams sqlAndParams = generateSqlAndParams(sqlId, msg, append);
        sendDB(AbstractDao.OPERATE, JsonObject.mapFrom(sqlAndParams), replyHandler, cls);
    }

    protected <T> void operate(String sqlId, Message<?> msg, BaseModel append, Handler<AsyncResult<Message<OperationResult<T>>>> replyHandler, Class<T> cls) {
        operate(sqlId, msg, new JsonObject(Json.encode(append)), replyHandler, cls);
    }

    protected <T> void batch(SqlAndParams sqlAndParams, Handler<AsyncResult<Message<List<T>>>> replyHandler, Class<T> cls) {
        sendDB(AbstractDao.BATCH, sqlAndParams, replyHandler, cls);
    }

    private void validateSqlAndParam(List<SqlAndParams> sqlAndParamsList) {
        if (sqlAndParamsList == null || sqlAndParamsList.isEmpty()) {
            throw new IllegalArgumentException("sqlAndParamsList 不能为空");
        }
    }

    /**
     * do one by one, rollback when the deal number is 0
     * sql "update ..." and its result is "deal number"
     * rollback when error occur
     * @param replyHandler []
     * @param sqlAndParamsList []
     */
    protected void transaction(List<SqlAndParams> sqlAndParamsList, Handler<AsyncResult<Message<Integer>>> replyHandler) {
        validateSqlAndParam(sqlAndParamsList);
        sendDB(AbstractDao.TRANSACTION,
                new JsonArray(sqlAndParamsList.stream().map(JsonObject::mapFrom).collect(Collectors.toList())),
                new DeliveryOptions().addHeader(Const.IGNORE, "0"),
                replyHandler, Integer.class);
    }


    /**
     * do one by one, no matter the deal number is 0
     * sql "update ..." and its result is "deal number"
     * rollback when error occur
     * @param replyHandler []
     * @param sqlAndParamsList []
     */
    protected void transactionIgnoreDealNum(List<SqlAndParams> sqlAndParamsList, Handler<AsyncResult<Message<Integer>>> replyHandler) {
        validateSqlAndParam(sqlAndParamsList);
        sendDB(AbstractDao.TRANSACTION,
                new JsonArray(sqlAndParamsList.stream().map(JsonObject::mapFrom).collect(Collectors.toList())),
                new DeliveryOptions().addHeader(Const.IGNORE, "1"),
                replyHandler,
                Integer.class);
    }

    private <T> void sendDB(String biz, Object obj, Handler<AsyncResult<Message<T>>> replyHandler, Class<?> cls) {
        sendDB(biz, obj, new DeliveryOptions(), replyHandler, cls);
    }

    private <T> void sendDB(String biz, Object obj, DeliveryOptions deliveryOptions, Handler<AsyncResult<Message<T>>> replyHandler, Class<?> cls) {
        if (cls != null) {
            deliveryOptions.addHeader(RETURN_CLASS_TYPE, cls.getName());
        }
        vertx.eventBus().<T>request(biz, obj, deliveryOptions, replyHandler);
    }

    protected <T> void redirect(String biz, Object obj, Handler<AsyncResult<Message<T>>> replyHandler) {
        redirect(biz, obj, new DeliveryOptions(), replyHandler);
    }

    private <T> void redirect(String biz, Object obj, DeliveryOptions deliveryOptions, Handler<AsyncResult<Message<T>>> replyHandler) {
        vertx.eventBus().request(biz, obj, deliveryOptions, replyHandler);
    }

    /**
     * 调用其他业务
     */
    protected <T> Future<T> callBiz(String biz) {
        return Future.future(reply -> callBiz(biz, getReplyHandler(reply)));
    }

    protected <T> Future<T> callBiz(String biz, Object o) {
        return Future.future(reply -> callBiz(biz, o, getReplyHandler(reply)));
    }

    protected <T> void callBiz(String biz, Handler<AsyncResult<Message<T>>> replyHandler) {
        callBiz(biz, null, new DeliveryOptions(), replyHandler);
    }

    protected <T> void callBiz(String biz, Object o, Handler<AsyncResult<Message<T>>> replyHandler) {
        callBiz(biz, o, new DeliveryOptions(), replyHandler);
    }

    protected <T> Future<T> callBizWithUser(LoginUser loginUser, String biz) {
        return Future.future(reply -> callBizWithUser(loginUser, biz, getReplyHandler(reply)));
    }

    protected <T> Future<T> callBizWithUser(LoginUser loginUser, String biz, Object o) {
        return Future.future(reply -> sendBizWithUser(loginUser, biz, o, getReplyHandler(reply)));
    }

    protected <T> void callBizWithUser(LoginUser loginUser, String biz, Handler<AsyncResult<Message<T>>> replyHandler) {
        DeliveryOptions deliveryOptions = new DeliveryOptions();
        deliveryOptions.addHeader(Const.LOGIN_USER, JsonObject.mapFrom(loginUser).encode());
        callBiz(biz, null, deliveryOptions, replyHandler);
    }

    protected <T> void sendBizWithUser(LoginUser loginUser, String biz, Object o, Handler<AsyncResult<Message<T>>> replyHandler) {
        DeliveryOptions deliveryOptions = new DeliveryOptions();
        deliveryOptions.addHeader(Const.LOGIN_USER, JsonObject.mapFrom(loginUser).encode());
        callBiz(biz, o, deliveryOptions, replyHandler);
    }

    private <T> void callBiz(String biz, Object jo, DeliveryOptions deliveryOptions, Handler<AsyncResult<Message<T>>> replyHandler) {
        MyVertx.vertx().eventBus().<T>request(biz, jo, deliveryOptions, replyHandler);
    }

    protected  <T> Handler<AsyncResult<Message<T>>> getReplyHandler(Promise<T> reply) {
        return messageReply -> {
            if (messageReply.succeeded()) {
                reply.complete(messageReply.result().body());
            } else {
                reply.fail(messageReply.cause());
            }
        };
    }

    protected <T> Handler<Throwable> throwableHandler(Message msg) {
        return err -> {
            log.error("throwableHandler", err);
            msg.fail(StatusCodeMsg.C500.getCode(), err.getMessage());
        };
    }

}
