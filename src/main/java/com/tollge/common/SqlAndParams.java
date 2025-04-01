package com.tollge.common;

import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class SqlAndParams extends BaseModel{
    /**
     * SQL语句关键字
     */
    private String sqlKey;
    // 参数
    private TollgeMap<String, Object> params;
    // 批量参数
    private List<TollgeMap<String, Object>> batchParams;

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
      params = new TollgeMap<>();
        params.putAll(jsonObject.getMap());
    }

    public SqlAndParams(String sqlKey, Integer currentPage, Integer pageSize) {
        this.sqlKey = sqlKey;

        limit = pageSize == null ? 20 : pageSize;
        offset = (currentPage == null ? 1 : currentPage - 1) * limit;
        if (offset < 0) {
            offset = 0;
        }
        if (limit < 0) {
            limit = 20;
        }
    }

    public SqlAndParams putParam(String key, Object value) {
        if (params == null) {
            params = new TollgeMap<>();
        }
        params.put(key, value);
        return this;
    }

    public SqlAndParams putParam(String key, String value) {
        if (params == null) {
            params = new TollgeMap<>();
        }
        params.put(key, value);
        return this;
    }

    public SqlAndParams putParam(String key, List value) {
        if (params == null) {
            params = new TollgeMap<>();
        }
        params.put(key, value);
        return this;
    }

    public SqlAndParams putParam(String key, Integer value) {
        if (params == null) {
            params = new TollgeMap<>();
        }
        params.put(key, value);
        return this;
    }
}
