package io.tollge.common.util;

import io.vertx.core.Vertx;

public class MyVertx {
    private MyVertx() {
    }

    public static Vertx vertx() {
        return Singleton.INSTANCE.getInstance().vertx;
    }

    public static void vertx(Vertx vertx) {
        if (Singleton.INSTANCE.getInstance().vertx == null) {
            Singleton.INSTANCE.getInstance().vertx = vertx;
        }
    }

    private enum Singleton {
        // 单例
        INSTANCE;

        private MyVertx single;

        private Singleton() {
            single = new MyVertx();
        }

        public MyVertx getInstance() {
            return single;
        }
    }

    private Vertx vertx;

}
