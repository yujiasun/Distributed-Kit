package com.distributed.utils;

import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.exceptions.JedisException;

/**
 * Created by sunyujia@aliyun.com on 2016/2/26.
 */
public class JedisUtils {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(JedisUtils.class);

    public static void closeResource(JedisPool jedisPool,Jedis jedis, boolean conectionBroken) {
        try {
            if (conectionBroken) {
                jedisPool.returnBrokenResource(jedis);
            } else {
                jedisPool.returnResource(jedis);
            }
        } catch (Exception e) {
            log.error("return back jedis failed, will fore close the jedis.", e);
            JedisUtils.destroyJedis(jedis);
        }
    }

    public static void destroyJedis(Jedis jedis) {
        if ((jedis != null) && jedis.isConnected()) {
            try {
                try {
                    jedis.quit();
                } catch (Exception e) {
                }
                jedis.disconnect();
            } catch (Exception e) {
            }
        }
    }

    public static boolean handleJedisException(JedisPool jedisPool,JedisException jedisException) {
        if (jedisException instanceof JedisConnectionException) {
            log.error("Redis connection  lost.", jedisException);
        } else if (jedisException instanceof JedisDataException) {
            if ((jedisException.getMessage() != null) && (jedisException.getMessage().indexOf("READONLY") != -1)) {
                log.error("Redis connection  are read-only slave.", jedisException);
            } else {
                // dataException, isBroken=false
                return false;
            }
        } else {
            log.error("Jedis exception happen.", jedisException);
        }
        return true;
    }
}