package com.tollge.common.util;

import com.tollge.common.TollgeException;
import com.tollge.common.auth.LoginUser;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

public class LoginUtil {

    private static final String ADMIN_ROLE = Properties.getString("application", "adminRole", "unknown");

    public static LoginUser getLoginUser(Message<?> msg) {
        LoginUser loginUser = Json.decodeValue(msg.headers().get(Const.LOGIN_USER), LoginUser.class);
        if(loginUser == null || loginUser.getUserId() == null) {
            throw new TollgeException("loginUser is null");
        }
        return loginUser;
    }

    public static LoginUser getLoginUser(RoutingContext ctx) {
        return ctx.get(Const.LOGIN_USER);
    }

    public static boolean isAdmin(LoginUser loginUser) {
        return loginUser.getRoleIdList().contains(Long.parseLong(ADMIN_ROLE));
    }
}
