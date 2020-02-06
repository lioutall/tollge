package com.tollge.common;

import io.vertx.core.json.Json;
import lombok.Data;

/**
 * @author Mirren
 */
@Data
public class ResultFormatObj {
    private boolean success;
    /**
     * 状态码
     */
    private int code;
    /**
     * 信息
     */
    private String message;
    /**
     * 数据
     */
    private Object data;

    public String toJsonStr() {
        return Json.encode(this);
    }

    public ResultFormatObj(boolean success, int status, String message, Object data) {
        super();
        this.success = success;
        this.message = message;
        this.code = status;
        this.data = data;
    }

}
