package cn.dev33.satoken.jboot;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.util.SaFoxUtil;
import io.jboot.components.serializer.JbootSerializer;
import io.jboot.utils.CacheUtil;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 使用Jboot的缓存方法存取Token数据
 */
public class SaTokenCacheDao implements SaTokenDao {

    protected SaRedisCache saRedisCache;
    protected JbootSerializer serializer;

    /**
     * 调用的Cache名称
     * @param cacheName 使用的缓存配置名，默认为 default
     */
    public SaTokenCacheDao(String cacheName) {
        saRedisCache = (SaRedisCache) CacheUtil.use(cacheName);
        serializer = new SaJdkSerializer();
    }


    @Override
    public String get(String key) {
        Jedis jedis = saRedisCache.getJedis();
        try {
            return jedis.get(key);
        } finally {
            saRedisCache.returnResource(jedis);
        }
    }

    @Override
    public void set(String key, String value, long timeout) {
        if (timeout == 0 || timeout <= SaTokenDao.NOT_VALUE_EXPIRE) {
            return;
        }
        Jedis jedis = saRedisCache.getJedis();
        try {
            if (timeout == SaTokenDao.NEVER_EXPIRE) {
                jedis.set(key, value);
            } else {
                jedis.setex(key, timeout, value);
            }
        } finally {
            saRedisCache.returnResource(jedis);
        }
    }

    @Override
    public void update(String key, String value) {
        long expire = getTimeout(key);
        // -2 = 无此键
        if (expire == SaTokenDao.NOT_VALUE_EXPIRE) {
            return;
        }
        this.set(key, value, expire);
    }

    @Override
    public void delete(String key) {
        Jedis jedis = saRedisCache.getJedis();
        try {
            jedis.del(key);
        } finally {
            saRedisCache.returnResource(jedis);
        }
    }

    @Override
    public long getTimeout(String key) {
        Jedis jedis = saRedisCache.getJedis();
        try {
            return jedis.ttl(key);
        } finally {
            saRedisCache.returnResource(jedis);
        }
    }

    @Override
    public void updateTimeout(String key, long timeout) {
        //判断是否想要设置为永久
        if (timeout == SaTokenDao.NEVER_EXPIRE) {
            long expire = getTimeout(key);
            if (expire == SaTokenDao.NEVER_EXPIRE) {
                // 如果其已经被设置为永久，则不作任何处理
            } else {
                // 如果尚未被设置为永久，那么再次set一次
                this.set(key, this.get(key), timeout);
            }
            return;
        }
        Jedis jedis = saRedisCache.getJedis();
        try {
            jedis.expire(key, timeout);
        } finally {
            saRedisCache.returnResource(jedis);
        }
    }

    @Override
    public Object getObject(String key) {
        Jedis jedis = saRedisCache.getJedis();
        try {
            return valueFromBytes(jedis.get(keyToBytes(key)));
        } finally {
            saRedisCache.returnResource(jedis);
        }
    }

    @Override
    public void setObject(String key, Object object, long timeout) {
        if (timeout == 0 || timeout <= SaTokenDao.NOT_VALUE_EXPIRE) {
            return;
        }
        Jedis jedis = saRedisCache.getJedis();
        try {
            if (timeout == SaTokenDao.NEVER_EXPIRE) {
                jedis.set(keyToBytes(key), valueToBytes(object));
            } else {
                jedis.setex(keyToBytes(key), timeout, valueToBytes(object));
            }
        } finally {
            saRedisCache.returnResource(jedis);
        }
    }

    @Override
    public void updateObject(String key, Object object) {
        long expire = getObjectTimeout(key);
        // -2 = 无此键
        if (expire == SaTokenDao.NOT_VALUE_EXPIRE) {
            return;
        }
        this.setObject(key, object, expire);
    }

    @Override
    public void deleteObject(String key) {
        Jedis jedis = saRedisCache.getJedis();
        try {
            jedis.del(keyToBytes(key));
        } finally {
            saRedisCache.returnResource(jedis);
        }
    }

    @Override
    public long getObjectTimeout(String key) {
        Jedis jedis = saRedisCache.getJedis();
        try {
            return jedis.ttl(keyToBytes(key));
        } finally {
            saRedisCache.returnResource(jedis);
        }
    }

    @Override
    public void updateObjectTimeout(String key, long timeout) {
        //判断是否想要设置为永久
        if (timeout == SaTokenDao.NEVER_EXPIRE) {
            long expire = getObjectTimeout(key);
            if (expire == SaTokenDao.NEVER_EXPIRE) {
                // 如果其已经被设置为永久，则不作任何处理
            } else {
                // 如果尚未被设置为永久，那么再次set一次
                this.setObject(key, this.getObject(key), timeout);
            }
            return;
        }
        Jedis jedis = saRedisCache.getJedis();
        try {
            jedis.expire(keyToBytes(key), timeout);
        } finally {
            saRedisCache.returnResource(jedis);
        }
    }

    @Override
    public SaSession getSession(String sessionId) {
        return SaTokenDao.super.getSession(sessionId);
    }

    @Override
    public void setSession(SaSession session, long timeout) {
        SaTokenDao.super.setSession(session, timeout);
    }

    @Override
    public void updateSession(SaSession session) {
        SaTokenDao.super.updateSession(session);
    }

    @Override
    public void deleteSession(String sessionId) {
        SaTokenDao.super.deleteSession(sessionId);
    }

    @Override
    public long getSessionTimeout(String sessionId) {
        return SaTokenDao.super.getSessionTimeout(sessionId);
    }

    @Override
    public void updateSessionTimeout(String sessionId, long timeout) {
        SaTokenDao.super.updateSessionTimeout(sessionId, timeout);
    }

    @Override
    public List<String> searchData(String prefix, String keyword, int start, int size) {
        Jedis jedis = saRedisCache.getJedis();
        try {
            Set<String> keys = jedis.keys(prefix + "*" + keyword + "*");
            List<String> list = new ArrayList<String>(keys);
            return SaFoxUtil.searchList(list, start, size);
        } finally {
            saRedisCache.returnResource(jedis);
        }
    }


    protected byte[] keyToBytes(Object key) {
        return key.toString().getBytes();
    }

    protected byte[] valueToBytes(Object value) {
        return serializer.serialize(value);
    }

    protected Object valueFromBytes(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        return serializer.deserialize(bytes);
    }
}
