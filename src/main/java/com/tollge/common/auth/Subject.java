package com.tollge.common.auth;

import com.tollge.common.TollgeException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 *
 */
public class Subject {

    private static final String SUBJECT_STORE = "SUBJECT_STORE";

    private LocalDateTime time = LocalDateTime.now();

    public LocalDateTime getTime() {
        return time;
    }

    public void refreshTime(LocalDateTime time) {
        this.time = time;
        authCustom.refreshTime(sessionID);
    }

    private String sessionID;

    private final AbstractAuth authCustom;

    public Subject(String sessionID, AbstractAuth authCustom) {
        this.sessionID = sessionID;
        this.authCustom = authCustom;
    }

    private volatile User authUser;

    /**
     * 用户登录
     *
     * @param ctx []
     * @param authInfo 鉴权信息
     * @param resultHandler 回调
     */
    public void login(RoutingContext ctx, JsonObject authInfo, Handler<AsyncResult<String>> resultHandler) {
        authCustom.login(ctx, authInfo, f->{
            if(f.succeeded()) {
                this.authUser = f.result();

                authCustom.removeSubject(sessionID, a -> {
                    if(a.succeeded()) {
                        String uuid = UUID.randomUUID().toString();
                        this.sessionID = uuid;
                        authCustom.addSubject(uuid, this, b -> {
                            if(b.succeeded()) {
                                authCustom.sendtoBrowser(ctx, uuid);
                                resultHandler.handle(Future.succeededFuture(uuid));
                            } else {
                                resultHandler.handle(Future.failedFuture(b.cause()));
                            }
                        });
                    } else {
                        resultHandler.handle(Future.failedFuture(a.cause()));
                    }
                });
            } else {
                resultHandler.handle(Future.failedFuture(f.cause()));
            }
        });

    }

    /**
     * 验证用户权限
     *
     * @param authority 权限
     * @param resultHandler 回调
     */
    public void isAuthorised(String authority, Handler<AsyncResult<Boolean>> resultHandler) {
        authUser.isAuthorized(authority, resultHandler);
    }

    /**
     * 用户注销
     * @param resultHandler []
     */
    public void logout(Handler<AsyncResult<Void>> resultHandler) {
        authCustom.removeSubject(this.sessionID, resultHandler);
    }

    public boolean isAuthenticated() {
        return this.authUser != null;
    }

    public User getUser() {
        return this.authUser;
    }

    public JsonObject getPrincipal() {
        return this.authUser == null ? null : this.authUser.principal();
    }

    public String getSessionID() {
        return sessionID;
    }

    public static Subject getCurrentSubject(RoutingContext rct) {
        Subject r = rct.get(SUBJECT_STORE);
        if(r == null) {
            throw new TollgeException("未找到登录的用户");
        }
        return r;
    }

    public void storeCurrentSubject(RoutingContext rct) {
        rct.put(SUBJECT_STORE, this);
    }

    public void kickUser(String key, Handler<AsyncResult<Boolean>> resultHandler) {
        authCustom.kickUser(key, resultHandler);
    }
}
