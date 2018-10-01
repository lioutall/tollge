package io.tollge.common.util;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;

import java.time.Duration;

class MyRedis {
        private MyRedis() {
        }

        static RedisClient getClient() {
            return Singleton.INSTANCE.getInstance().client;
        }

        private enum Singleton {
            // 单例
            INSTANCE;

            private MyRedis single;

            private Singleton() {
                single = new MyRedis();
                String group = "redis";
                RedisURI uri = RedisURI.create(Properties.getString(group, "ip"),Properties.getInteger(group, "port"));
                uri.setPassword(Properties.getString(group, "pass"));
                uri.setTimeout(Duration.ofSeconds(2));
                single.client = RedisClient.create(uri);
            }

            public MyRedis getInstance() {
                return single;
            }
        }

        private RedisClient client;

}
