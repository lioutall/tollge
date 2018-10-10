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
import com.tollge.common.annotation.valid.NotNull;
import com.tollge.common.annotation.valid.NotNulls;
import io.netty.util.internal.StringUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static com.tollge.common.annotation.Type.*;

@Slf4j
public class BizVerticle extends AbstractVerticle {

    private final BiFunction<Message<JsonObject>, SqlAndParams, Handler<AsyncResult<Message<Object>>>> bizResultHandler = (msg, sqlAndParams) -> res -> {
        if (res.succeeded()) {
            msg.reply(res.result().body());
        } else {
            log.error("BizVerticle failed:{}", sqlAndParams, res.cause());
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

                JsonObject body = msg.body();
                boolean hasBody = body != null;
                for (Annotation annotation : m.getDeclaredAnnotations()) {
                    // 校验
                    if (annotation instanceof NotNulls) {
                        validate(body, hasBody, (NotNulls) annotation);
                    }
                    if (annotation instanceof NotNull) {
                        validate(body, hasBody, (NotNull) annotation);
                    }

                    // 初始化
                    if (annotation instanceof InitIfNulls && hasBody) {
                        init(body, (InitIfNulls) annotation);
                    }
                    if (annotation instanceof InitIfNull && hasBody) {
                        init(body, (InitIfNull) annotation);
                    }

                    // 类型转换
                    if (annotation instanceof ChangeTypes && hasBody) {
                        changeType(body, (ChangeTypes) annotation);
                    }
                    if (annotation instanceof ChangeType && hasBody) {
                        changeType(body, (ChangeType) annotation);
                    }
                }

                access.invoke(this, methodIndex, msg);

            });
            log.info("服务 {}", path);
        });
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

    private void validate(JsonObject body, boolean hasBody, NotNulls annotation) {
        NotNulls notNulls = annotation;
        NotNull[] ns = notNulls.value();
        for (NotNull n : ns) {
            validate(body, hasBody, n);
        }
    }

    private void validate(JsonObject body, boolean hasBody, NotNull n) {
        Object value = body.getValue(n.key());
        if (value instanceof String) {
            Preconditions.checkArgument(hasBody
                    && !StringUtil.isNullOrEmpty(body.getString(n.key())), n.msg());
        } else {
            Preconditions.checkArgument(hasBody && value != null, n.msg());
        }


    }

    /**********************
     *  以下是sql方法封装, 便于使用
     */
    protected void one(Message<JsonObject> msg, SqlAndParams sqlAndParams) {
        vertx.eventBus().send(AbstractDao.ONE, JsonObject.mapFrom(sqlAndParams), bizResultHandler.apply(msg, sqlAndParams));
    }

    protected void one(String sqlId, Message<JsonObject> msg, JsonObject append) {
        base(sqlId, msg, append, AbstractDao.ONE);
    }

    private void base(String sqlId, Message<JsonObject> msg, JsonObject append, String daoId) {
        SqlAndParams sqlAndParams = new SqlAndParams(sqlId);
        JsonObject o = msg.body();
        if (o != null && o.size() > 0) {
            o.forEach(entry -> sqlAndParams.putParam(entry.getKey(), entry.getValue()));
        }

        if (append != null && append.size() > 0) {
            append.forEach(entry -> sqlAndParams.putParam(entry.getKey(), entry.getValue()));
        }

        vertx.eventBus().send(daoId, JsonObject.mapFrom(sqlAndParams), bizResultHandler.apply(msg, sqlAndParams));
    }

    protected void count(Message<JsonObject> msg, SqlAndParams sqlAndParams) {
        vertx.eventBus().send(AbstractDao.COUNT, JsonObject.mapFrom(sqlAndParams), bizResultHandler.apply(msg, sqlAndParams));
    }

    protected void count(String sqlId, Message<JsonObject> msg, JsonObject append) {
        base(sqlId, msg, append, AbstractDao.COUNT);
    }

    /**
     * 传了分页参数, 就走分页流程
     *
     * @param msg
     * @param sqlAndParams
     */
    protected void list(Message<JsonObject> msg, SqlAndParams sqlAndParams) {
        // 如果是分页
        vertx.eventBus().send(AbstractDao.LIST, JsonObject.mapFrom(sqlAndParams), bizResultHandler.apply(msg, sqlAndParams));
    }

    protected void page(Message<JsonObject> msg, SqlAndParams sqlAndParams) {
        vertx.eventBus().send(AbstractDao.PAGE, JsonObject.mapFrom(sqlAndParams), bizResultHandler.apply(msg, sqlAndParams));
    }

    protected void list(String sqlId, Message<JsonObject> msg, JsonObject append, Handler<AsyncResult<Message<Object>>> replyHandler) {
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

        vertx.eventBus().send(daoId, JsonObject.mapFrom(sqlAndParams), replyHandler);
    }

    protected void list(String sqlId, Message<JsonObject> msg, JsonObject append) {
        list(sqlId, msg, append, bizResultHandler.apply(msg, new SqlAndParams(sqlId)));
    }


    protected void operate(Message<JsonObject> msg, SqlAndParams sqlAndParams) {
        vertx.eventBus().send(AbstractDao.OPERATE, JsonObject.mapFrom(sqlAndParams), bizResultHandler.apply(msg, sqlAndParams));
    }

    protected void operate(String sqlId, Message<JsonObject> msg, JsonObject append) {
        base(sqlId, msg, append, AbstractDao.OPERATE);
    }

    protected void batch(Message<JsonObject> msg, List<SqlAndParams> sqlAndParamsList) {
        validateSqlAndParam(sqlAndParamsList);
        vertx.eventBus().send(AbstractDao.BATCH, new JsonArray(sqlAndParamsList), bizResultHandler.apply(msg, sqlAndParamsList.get(0)));
    }

    protected void transaction(Message<JsonObject> msg, List<SqlAndParams> sqlAndParamsList) {
        validateSqlAndParam(sqlAndParamsList);
        vertx.eventBus().send(AbstractDao.TRANSACTION,
                new JsonArray(sqlAndParamsList.stream().map(JsonObject::mapFrom).collect(Collectors.toList())),
                bizResultHandler.apply(msg, sqlAndParamsList.get(0)));
    }

    private void validateSqlAndParam(List<SqlAndParams> sqlAndParamsList) {
        if (sqlAndParamsList == null || sqlAndParamsList.isEmpty()) {
            throw new IllegalArgumentException("sqlAndParamsList 不能为空");
        }
    }

    protected void transaction(List<SqlAndParams> sqlAndParamsList, Handler<AsyncResult<Message<Integer>>> replyHandler) {
        validateSqlAndParam(sqlAndParamsList);
        vertx.eventBus().send(AbstractDao.TRANSACTION,
                new JsonArray(sqlAndParamsList.stream().map(JsonObject::mapFrom).collect(Collectors.toList())),
                replyHandler);
    }

}
