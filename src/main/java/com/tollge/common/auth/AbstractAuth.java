package com.tollge.common.auth;

import com.google.common.collect.ImmutableSet;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;

public abstract class AbstractAuth {

    /**
     * 新增subject到缓存
     * @param key []
     * @param subject []
     * @param resultHandler []
     */
    public abstract void addSubject(String key, Subject subject, Handler<AsyncResult<String>> resultHandler);

    /**
     * 从缓存获取
     * @param key []
     * @param resultHandler []
     */
    public abstract void getSubject(String key, Handler<AsyncResult<Subject>> resultHandler);

    /**
     * 移除缓存
     * @param key []
     * @param resultHandler []
     */
    public abstract void removeSubject(String key, Handler<AsyncResult<Void>> resultHandler);

    /**
     * 定时清除缓存
     * 需要定时器,返回true
     * @return 操作结果
     */
    public abstract boolean clearSubjects();

    /**
     * 鉴权成功, 把鉴权关键字通知浏览器
     * @param ctx []
     * @param sessionKey []
     */
    public abstract void sendtoBrowser(RoutingContext ctx, String sessionKey);

    /**
     * 从请求里取 鉴权关键字
     * @param ctx []
     * @return 鉴权关键字
     */
    public abstract String fetchFromBrowser(RoutingContext ctx);

    /**
     * 登录
     * @param ctx []
     * @param authInfo []
     * @param resultHandler []
     */
    public abstract void login(RoutingContext ctx, JsonObject authInfo, Handler<AsyncResult<User>> resultHandler);

    /**
     * 获取匿名权限列表
     * @param resultHandler []
     */
    public abstract void getAnnoPremissions(Handler<AsyncResult<ImmutableSet<String>>> resultHandler);
}
