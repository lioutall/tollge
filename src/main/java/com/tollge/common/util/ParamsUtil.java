package com.tollge.common.util;


import io.netty.util.internal.StringUtil;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

/**
 * params字段工具类
 */
public class ParamsUtil {

    public static void moveInto(Message<JsonObject> msg, String... keys) {
        JsonObject body = msg.body();
        if (body != null && keys != null) {
            JsonObject params = new JsonObject();
            if(body.containsKey(Const.PARAMS)) {
                params = new JsonObject(body.getString(Const.PARAMS));
            }
            body.put(Const.PARAMS, params);

            for (String key : keys) {
                if (!StringUtil.isNullOrEmpty(key) && body.containsKey(key)) {
                    params.put(key, body.getValue(key));
                    body.remove(key);
                }
            }
        }
    }

    public static void json2String(Message<JsonObject> msg) {
        JsonObject body = msg.body();
        if (body != null && body.containsKey(Const.PARAMS)) {
            body.put(Const.PARAMS, body.getJsonObject(Const.PARAMS).toString());
        }
    }

}
