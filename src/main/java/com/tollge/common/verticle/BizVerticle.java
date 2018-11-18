package com.tollge.common.verticle;

import com.esotericsoftware.reflectasm.MethodAccess;
import com.google.common.base.Preconditions;
import com.tollge.common.SqlAndParams;
import com.tollge.common.StatusCodeMsg;
import com.tollge.common.annotation.data.ChangeType;
import com.tollge.common.annotation.data.ChangeTypes;
import com.tollge.common.annotation.data.InitIfNull;
import com.tollge.common.annotation.data.InitIfNulls;
import com.tollge.common.annotation.mark.Biz;
import com.tollge.common.annotation.mark.Path;
import com.tollge.common.annotation.valid.*;
import com.tollge.common.util.Const;
import io.netty.util.internal.StringUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
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

/**
 * 业务逻辑抽象类
 * @author tollge
 */
@Slf4j
public class BizVerticle extends AbstractVerticle {

    private final BiFunction<Message<JsonObject>, SqlAndParams, Handler<AsyncResult<Message<Object>>>> bizResultHandler = (msg, sqlAndParams) -> res -> {
        if (res.succeeded()) {
            msg.reply(res.result().body());
        } else {
            if (res.cause() instanceof IllegalArgumentException) {
                msg.fail(StatusCodeMsg.C414.getCode(), res.cause().getMessage());
            } else {
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
            vertx.eventBus().<JsonObject>consumer(path, msg -> {
                try {
                    // 业务前校验和初始化
                    validateAndInit(m, msg);
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
                            body.put(t.key(), new Double(body.getString(t.key())));
                            break;
                        case INTEGER:
                            body.put(t.key(), new Double(body.getInteger(t.key())));
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
        InitIfNulls initIfNulls = annotation;
        InitIfNull[] is = initIfNulls.value();
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
                    && v.matches(body.getString(n.regex())), n.msg());
        } else {
            throw new IllegalArgumentException(n.key() + "is not string");
        }
    }

    /**********************
     *  以下是sql方法封装, 便于使用

     * @param msg []
     * @param sqlAndParams []
     */
    protected void one(Message<JsonObject> msg, SqlAndParams sqlAndParams) {
        sendDB(AbstractDao.ONE, JsonObject.mapFrom(sqlAndParams), bizResultHandler.apply(msg, sqlAndParams));
    }

    protected void one(String sqlId, Message<JsonObject> msg, JsonObject append) {
        SqlAndParams sqlAndParams = generateSqlAndParams(sqlId, msg, append);
        sendDB(AbstractDao.ONE, JsonObject.mapFrom(sqlAndParams), bizResultHandler.apply(msg, sqlAndParams));
    }

    protected <T> void one(String sqlId, Message<JsonObject> msg, JsonObject append, Handler<AsyncResult<Message<T>>> replyHandler) {
        SqlAndParams sqlAndParams = generateSqlAndParams(sqlId, msg, append);
        sendDB(AbstractDao.ONE, JsonObject.mapFrom(sqlAndParams), replyHandler);
    }

    private SqlAndParams generateSqlAndParams(String sqlId, Message<JsonObject> msg, JsonObject append) {
        SqlAndParams sqlAndParams = new SqlAndParams(sqlId);
        JsonObject o = msg.body();
        if (o != null && o.size() > 0) {
            o.forEach(entry -> sqlAndParams.putParam(entry.getKey(), entry.getValue()));
        }

        if (append != null && append.size() > 0) {
            append.forEach(entry -> sqlAndParams.putParam(entry.getKey(), entry.getValue()));
        }
        return sqlAndParams;
    }

    protected void count(Message<JsonObject> msg, SqlAndParams sqlAndParams) {
        sendDB(AbstractDao.COUNT, JsonObject.mapFrom(sqlAndParams), bizResultHandler.apply(msg, sqlAndParams));
    }

    protected void count(String sqlId, Message<JsonObject> msg, JsonObject append) {
        SqlAndParams sqlAndParams = generateSqlAndParams(sqlId, msg, append);
        sendDB(AbstractDao.COUNT, JsonObject.mapFrom(sqlAndParams), bizResultHandler.apply(msg, sqlAndParams));
    }

    protected <T> void count(String sqlId, Message<JsonObject> msg, JsonObject append, Handler<AsyncResult<Message<T>>> replyHandler) {
        SqlAndParams sqlAndParams = generateSqlAndParams(sqlId, msg, append);
        sendDB(AbstractDao.COUNT, JsonObject.mapFrom(sqlAndParams), replyHandler);
    }

    /**
     * 传了分页参数, 就走分页流程
     *
     * @param msg []
     * @param sqlAndParams []
     */
    protected void list(Message<JsonObject> msg, SqlAndParams sqlAndParams) {
        sendDB(AbstractDao.LIST, JsonObject.mapFrom(sqlAndParams), bizResultHandler.apply(msg, sqlAndParams));
    }

    protected void page(Message<JsonObject> msg, SqlAndParams sqlAndParams) {
        sendDB(AbstractDao.PAGE, JsonObject.mapFrom(sqlAndParams), bizResultHandler.apply(msg, sqlAndParams));
    }

    protected void list(String sqlId, Message<JsonObject> msg, JsonObject append) {
        list(sqlId, msg, append, bizResultHandler.apply(msg, new SqlAndParams(sqlId)));
    }

    protected <T> void list(String sqlId, Message<JsonObject> msg, JsonObject append, Handler<AsyncResult<Message<T>>> replyHandler) {
        SqlAndParams sqlAndParams = new SqlAndParams(sqlId);
        String daoId = AbstractDao.LIST;
        JsonObject o = msg.body();
        if (o != null && o.size() > 0) {
            // 如果是分页
            if (o.containsKey("currentPage") && o.containsKey("pageSize")) {
                sqlAndParams = new SqlAndParams(sqlId, Integer.valueOf(o.getString("currentPage")),
                        Integer.valueOf(o.getString("pageSize")));
                daoId = AbstractDao.PAGE;
                o.remove("currentPage");
                o.remove("pageSize");
            }

            for (Map.Entry<String, Object> entry : o) {
                sqlAndParams.putParam(entry.getKey(), entry.getValue());
            }
        }

        if (append != null && append.size() > 0) {
            for (Map.Entry<String, Object> entry : append) {
                sqlAndParams.putParam(entry.getKey(), entry.getValue());
            }
        }

        sendDB(daoId, JsonObject.mapFrom(sqlAndParams), replyHandler);
    }

    protected void operate(Message<JsonObject> msg, SqlAndParams sqlAndParams) {
        sendDB(AbstractDao.OPERATE, JsonObject.mapFrom(sqlAndParams), bizResultHandler.apply(msg, sqlAndParams));
    }

    protected void operate(String sqlId, Message<JsonObject> msg, JsonObject append) {
        SqlAndParams sqlAndParams = generateSqlAndParams(sqlId, msg, append);
        sendDB(AbstractDao.OPERATE, JsonObject.mapFrom(sqlAndParams), bizResultHandler.apply(msg, sqlAndParams));
    }

    protected <T> void operate(String sqlId, Message<JsonObject> msg, JsonObject append, Handler<AsyncResult<Message<T>>> replyHandler) {
        SqlAndParams sqlAndParams = generateSqlAndParams(sqlId, msg, append);
        sendDB(AbstractDao.OPERATE, JsonObject.mapFrom(sqlAndParams), replyHandler);
    }

    protected void batch(Message<JsonObject> msg, List<SqlAndParams> sqlAndParamsList) {
        validateSqlAndParam(sqlAndParamsList);
        sendDB(AbstractDao.BATCH, new JsonArray(sqlAndParamsList), bizResultHandler.apply(msg, sqlAndParamsList.get(0)));
    }

    protected <T> void batch(List<SqlAndParams> sqlAndParamsList, Handler<AsyncResult<Message<T>>> replyHandler) {
        validateSqlAndParam(sqlAndParamsList);
        sendDB(AbstractDao.BATCH, new JsonArray(sqlAndParamsList), replyHandler);
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
     * @param msg []
     * @param sqlAndParamsList []
     */
    protected void transaction(Message<JsonObject> msg, List<SqlAndParams> sqlAndParamsList) {
        validateSqlAndParam(sqlAndParamsList);
        sendDB(AbstractDao.TRANSACTION,
                new JsonArray(sqlAndParamsList.stream().map(JsonObject::mapFrom).collect(Collectors.toList())),
                new DeliveryOptions().addHeader(Const.IGNORE, "0"),
                bizResultHandler.apply(msg, sqlAndParamsList.get(0)));
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
                replyHandler);
    }

    /**
     * do one by one, no matter the deal number is 0
     * sql "update ..." and its result is "deal number"
     * rollback when error occur
     * @param msg []
     * @param sqlAndParamsList []
     */
    protected void transactionIgnoreDealNum(Message<JsonObject> msg, List<SqlAndParams> sqlAndParamsList) {
        validateSqlAndParam(sqlAndParamsList);
        sendDB(AbstractDao.TRANSACTION,
                new JsonArray(sqlAndParamsList.stream().map(JsonObject::mapFrom).collect(Collectors.toList())),
                new DeliveryOptions().addHeader(Const.IGNORE, "1"),
                bizResultHandler.apply(msg, sqlAndParamsList.get(0)));
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
                replyHandler);
    }

    private <T> void sendDB(String biz, Object obj, Handler<AsyncResult<Message<T>>> replyHandler) {
        sendDB(biz, obj, new DeliveryOptions(), replyHandler);
    }

    private <T> void sendDB(String biz, Object obj, DeliveryOptions deliveryOptions, Handler<AsyncResult<Message<T>>> replyHandler) {
        vertx.eventBus().<T>send(biz, obj, deliveryOptions, replyHandler);
    }

    protected <T> void redirect(String biz, Object obj, Handler<AsyncResult<Message<T>>> replyHandler) {
        redirect(biz, obj, new DeliveryOptions(), replyHandler);
    }

    private <T> void redirect(String biz, Object obj, DeliveryOptions deliveryOptions, Handler<AsyncResult<Message<T>>> replyHandler) {
        vertx.eventBus().send(biz, obj, deliveryOptions, replyHandler);
    }

}
