package com.example.MeetingsRedis.model;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisConnection {

    private static final JedisPool jedisPool;

    static {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(128);
        jedisPool = new JedisPool(poolConfig, "127.0.0.1", 6379);
    }

    public static Jedis getResource() {
        return jedisPool.getResource();
    }

    public static void close() {
        jedisPool.close();
    }
}
