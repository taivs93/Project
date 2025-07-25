    package com.taivs.project.service.redis;

    import jakarta.annotation.PostConstruct;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.data.redis.core.RedisTemplate;
    import org.springframework.stereotype.Service;

    import java.time.Duration;

    @Service
    public class RedisServiceImpl implements RedisService {

        @Autowired
        private RedisTemplate<String, Object> redisTemplate;

        @PostConstruct
        public void testRedis() {
            System.out.println("Redis host: " + redisTemplate.getConnectionFactory().getConnection().getClientName());
        }

        @Override
        public <T> void set(String key, T value, Duration ttl) {
            redisTemplate.opsForValue().set(key, value, ttl);
        }

        @Override
        public <T> T get(String key, Class<T> clazz) {
            try {
                System.out.println("Test Get");
                Object val = redisTemplate.opsForValue().get(key);
                System.out.println("Check test");
                return clazz.cast(val);
            } catch (Exception e) {
                System.out.println("ERROR WHEN GET FROM REDIS:");
                e.printStackTrace();
                return null;
            }
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

