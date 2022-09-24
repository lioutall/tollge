package com.tollge.common;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 将StatusCodeMsg转换为返回json<br>
 * 返回结果<br>
 * status : 状态码<br>
 * msg : 信息<br>
 * data : 数据<br>
 *
 * @author Mirren
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResultFormat {

    public static final String CODE = "code";
    public static final String SUCCESS = "success";
    public static final String MESSAGE = "message";
    public static final String DATA = "data";

    /**
     * 格式化返回结果,code为状态码枚举类,data为数据
     *
     * @param code 状态码
     * @param data 数据
     * @return 格式化返回
     */
    public static String format(StatusCodeMsg code, Object data) {
        if (data instanceof JsonArray) return format(code, (JsonArray) data);
        if (data instanceof JsonObject) return format(code, (JsonObject) data);
        ResultFormatObj result = new ResultFormatObj(StatusCodeMsg.C200.equals(code), code.getCode(), code.getMsg(), data);
        return result.toJsonStr();
    }

    /**
     * 格式化返回结果,code为状态码枚举类,data为数据
     *
     * @param code 状态码
     * @param data 数据
     * @return 格式化返回
     */
    public static String format(StatusCodeMsg code, JsonArray data) {
        if (data == null) {
            data = new JsonArray();
        }
        return result(code, data);
    }

    /**
     * 格式化返回结果,code为状态码枚举类,data为数据
     *
     * @param code 状态码
     * @param data 数据
     * @return 格式化返回
     */
    public static String format(StatusCodeMsg code, JsonObject data) {
        if (data == null) {
            data = new JsonObject();
        }
        return result(code, data);
    }

    /**
     * 格式化返回结果其中data为null,code为状态码枚举类
     *
     * @param code 状态码
     * @return 格式化返回
     */
    public static String formatAsNull(StatusCodeMsg code) {
        return resultWithoutData(code);
    }

    private static String result(StatusCodeMsg code, Object data) {
        JsonObject result = new JsonObject();
        result.put(SUCCESS, StatusCodeMsg.C200.equals(code));
        result.put(CODE, code.getCode());
        result.put(MESSAGE, code.getMsg());
        result.put(DATA, data);
        return result.toString();
    }

    private static String resultWithoutData(StatusCodeMsg code) {
        JsonObject result = new JsonObject();
        result.put(SUCCESS, StatusCodeMsg.C200.equals(code));
        result.put(CODE, code.getCode());
        result.put(MESSAGE, code.getMsg());
        return result.toString();
    }

    /**
     * 格式化返回结果,code为状态码枚举类,data为数据
     *
     * @param code 状态码
     * @param message 错误信息
     * @return 格式化返回
     */
    public static String formatError(StatusCodeMsg code, String message) {
        return resultWithoutData(code, message);
    }

    private static String resultWithoutData(StatusCodeMsg code, String message) {
        JsonObject result = new JsonObject();
        result.put(SUCCESS, StatusCodeMsg.C200.equals(code));
        result.put(CODE, code.getCode());
        result.put(MESSAGE, message);
        return result.toString();
    }
}
