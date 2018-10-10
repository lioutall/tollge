package com.tollge.common.util;


import com.alicp.jetcache.Cache;
import com.alicp.jetcache.MultiLevelCacheBuilder;
import com.alicp.jetcache.embedded.CaffeineCacheBuilder;
import com.alicp.jetcache.redis.lettuce.RedisLettuceCacheBuilder;
import com.alicp.jetcache.support.FastjsonKeyConvertor;
import com.alicp.jetcache.support.JavaValueDecoder;
import com.alicp.jetcache.support.JavaValueEncoder;
import io.lettuce.core.RedisClient;
import lombok.NoArgsConstructor;

import java.util.concurrent.TimeUnit;


/**
 * 缓存工具类
 * 只提供了常用的缓存创建
 * 最好是直接使用jetcache
 */
@NoArgsConstructor
public class CacheUtil {

    public static <K, V> Cache<K, V> buildLocalCache(int limit, long expireSeconds) {
        return CaffeineCacheBuilder.createCaffeineCacheBuilder()
                .limit(limit)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .buildCache();
    }

    public static <K, V> Cache<K, V> buildRemoteCache(String prefix, long expireSeconds) {
        return RedisLettuceCacheBuilder.createRedisLettuceCacheBuilder()
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .valueEncoder(JavaValueEncoder.INSTANCE)
                .valueDecoder(JavaValueDecoder.INSTANCE)
                .redisClient(MyRedis.getClient())
                .keyPrefix(prefix)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .buildCache();
    }

    public static <K, V> Cache<K, V> buildMultiLevelCache(int localLimit, String prefix, int expireSeconds) {
        Cache<String, String> localCache = CaffeineCacheBuilder.createCaffeineCacheBuilder()
                .limit(localLimit)
                .buildCache();

        Cache<String, String> remoteCache = RedisLettuceCacheBuilder.createRedisLettuceCacheBuilder()
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .valueEncoder(JavaValueEncoder.INSTANCE)
                .valueDecoder(JavaValueDecoder.INSTANCE)
                .redisClient(MyRedis.getClient())
                .keyPrefix(prefix)
                .buildCache();

        return MultiLevelCacheBuilder.createMultiLevelCacheBuilder()
                .addCache(localCache, remoteCache)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .buildCache();
    }

    public static RedisClient getRedisClient() {
        return MyRedis.getClient();
    }
}
