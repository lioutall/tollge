package com.tollge.common.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

/**
 * 数据库调用综合Verticle 抽象
 *
 * @author toyer
 */
@Slf4j
public abstract class AbstractDao extends AbstractVerticle {
    protected static final String GET_REAL_SQL_FAILED = "getRealSql failed, sqlKey={}";

    /**
     * return: numerical value
     */
    public static final String COUNT = "dao://count";
    /**
     * return: JsonArray(via return) or deal number
     */
    public static final String OPERATE = "dao://operate";
    /**
     * batch operation
     * return: deal number
     */
    public static final String BATCH = "dao://batch";
    /**
     * return: JsonObject
     */
    public static final String ONE = "dao://one";
    /**
     * return: JsonArray
     */
    public static final String LIST = "dao://list";
    /**
     * return: page
     */
    public static final String PAGE = "dao://page";
    /**
     * execute via transaction
     * two modes: all success(deal number > 0);  ignore deal number
     */
    public static final String TRANSACTION = "dao://transaction";

    @Override
    public void start() {
        init();
        vertx.eventBus().consumer(COUNT, this::count);
        vertx.eventBus().consumer(OPERATE, this::operate);
        vertx.eventBus().consumer(BATCH, this::batch);
        vertx.eventBus().consumer(ONE, this::one);
        vertx.eventBus().consumer(LIST, this::list);
        vertx.eventBus().consumer(PAGE, this::page);
        vertx.eventBus().consumer(TRANSACTION, this::transaction);
    }

    protected abstract void init();

    protected abstract void count(Message<JsonObject> msg);

    protected abstract void page(Message<JsonObject> msg);

    protected abstract void list(Message<JsonObject> msg);

    protected abstract void one(Message<JsonObject> msg);

    protected abstract void operate(Message<JsonObject> msg);

    protected abstract void batch(Message<JsonArray> msg);

    protected abstract void transaction(Message<JsonArray> msg);

    protected abstract JsonObject getDbConfig();

}
