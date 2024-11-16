package com.tollge.common.util;

import com.tollge.common.TollgeException;
import com.tollge.common.auth.LoginUser;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

public class LoginUtil {
    public static LoginUser getLoginUser(Message<?> msg) {
        LoginUser loginUser = Json.decodeValue(msg.headers().get(Const.LOGIN_USER), LoginUser.class);
        if(loginUser == null || loginUser.getUserId() == null) {
            throw new TollgeException("loginUser is null");
        }
        return loginUser;
    }
}
