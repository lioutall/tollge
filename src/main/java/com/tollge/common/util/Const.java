package com.tollge.common.util;

import io.vertx.core.json.JsonObject;

public class Const {
    public static final String CHILDREN = "children";
    public static final String PARAMS = "params";
    public static final String STATUS = "status";
    public static final String CURRENT_USER = "currentUser";
    public static final String LOGIN_USER = "loginUser";
    public static final String AUTH_CUSTOM= "authCustom";
    public static final String CURRENT_USER_ID = "currentUserId";
    public static final String ID = "id";

    public static final String TOLLGE_PAGE_COUNT = "X-Total-Count";
    public static final String TOLLGE_PAGE_DATA = "data";
    public static final String IGNORE = "ignore";

    public static final String DEFAULT_CONTENT_TYPE = "application/json";


    public static final String RETURN_CLASS_TYPE = "TOLLGE_RETURN_CLASS_TYPE";

    public static final JsonObject NULL_JSON = null;
    public static final JsonObject EMPTY_JSON = new JsonObject();
}
