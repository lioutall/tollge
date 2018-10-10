package com.tollge.common;

import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
public class SqlAndParams {
    /**
     * SQL语句关键字
     */
    private String sqlKey;
    // 参数
    private Map<String, Object> params;
    // 批量参数
    private List<Map<String, String>> batchParams;

    // 分页参数
    private int limit;
    private int offset;

    public SqlAndParams() {
    }

    public SqlAndParams(String sqlKey) {
        this.sqlKey = sqlKey;
    }

    public SqlAndParams(String sqlKey, JsonObject jsonObject) {
        this.sqlKey = sqlKey;
        params = jsonObject.getMap();
    }

    public SqlAndParams(String sqlKey, Integer currentPage, Integer pageSize) {
        this.sqlKey = sqlKey;

        limit = pageSize == null ? 20 : pageSize;
        offset = (currentPage == null ? 1 : currentPage - 1) * limit;
    }

    public SqlAndParams putParam(String key, Object value) {
        if (params == null) {
            params = new HashMap<>();
        }
        params.put(key, value);
        return this;
    }

    public SqlAndParams putParam(String key, String value) {
        if (params == null) {
            params = new HashMap<>();
        }
        params.put(key, value);
        return this;
    }

    public SqlAndParams putParam(String key, List value) {
        if (params == null) {
            params = new HashMap<>();
        }
        params.put(key, value);
        return this;
    }

    public SqlAndParams putParam(String key, Integer value) {
        if (params == null) {
            params = new HashMap<>();
        }
        params.put(key, value);
        return this;
    }
}
