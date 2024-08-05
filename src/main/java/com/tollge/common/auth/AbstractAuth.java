package com.tollge.common.auth;

import com.google.common.collect.ImmutableSet;
import com.tollge.common.ResultFormat;
import com.tollge.common.StatusCodeMsg;
import com.tollge.common.util.Const;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.Set;

public abstract class AbstractAuth {

    /**
     * 新增user到缓存
     * @param key []
     * @param user []
     * @param resultHandler []
     */
    public abstract void cacheLoginUser(String key, LoginUser user, Handler<AsyncResult<Boolean>> resultHandler);

    /**
     * 从缓存获取
     * @param key []
     * @param resultHandler []
     */
    public abstract void getLoginUser(String key, Handler<AsyncResult<LoginUser>> resultHandler);

    /**
     * 移除缓存
     * @param key []
     * @param resultHandler []
     */
    public abstract void removeLoginUser(String key, Handler<AsyncResult<Boolean>> resultHandler);

    /**
     * 定时清除缓存
     * 需要定时器,返回true
     * @return 操作结果
     */
    public abstract boolean clearLoginUser();

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
     * 获取匿名权限列表
     * @param resultHandler []
     */
    public abstract void getAnnoPermissions(Handler<AsyncResult<Set<String>>> resultHandler);

    /**
     * 校验权限
     * @param resultHandler .
     */
    public abstract void checkPermission(String permission, RoutingContext ctx, Handler<AsyncResult<Boolean>> resultHandler);

    /**
     * 用户不具备访问permission权限处理
     * @param ctx []
     */
    public void failAuthenticate(RoutingContext ctx) {
        ctx.response().putHeader("content-type", Const.DEFAULT_CONTENT_TYPE).end(ResultFormat.format(StatusCodeMsg.C314, new JsonObject()));
    }

    /**
     * 用户不存在或登录失败处理
     * @param ctx []
     */
    public void failLogin(RoutingContext ctx) {
        ctx.response().putHeader("content-type", Const.DEFAULT_CONTENT_TYPE).end(ResultFormat.format(StatusCodeMsg.C300, new JsonObject()));
    }

    /**
     * 踢出关键字的用户
     * @param key .
     * @param resultHandler .
     */
    public abstract void kickLoginUser(String key, Handler<AsyncResult<Boolean>> resultHandler);
}
