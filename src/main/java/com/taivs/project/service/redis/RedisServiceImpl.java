    package com.taivs.project.service.redis;

    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.data.redis.core.RedisTemplate;
    import org.springframework.stereotype.Service;

    import java.time.Duration;

    @Service
    public class RedisServiceImpl implements RedisService {

        @Autowired
        private RedisTemplate<String, Object> redisTemplate;

        @Override
        public <T> void set(String key, T value, Duration ttl) {
            redisTemplate.opsForValue().set(key, value, ttl);
        }

        @Override
        public <T> T get(String key, Class<T> clazz) {
            Object val = redisTemplate.opsForValue().get(key);
            return clazz.cast(val);
        }

        @Override
        public boolean exists(String key) {
            return redisTemplate.hasKey(key);
        }

        @Override
        public void delete(String key) {
            redisTemplate.delete(key);
        }
    }

