package taivs.project.service.redis;

import java.time.Duration;

public interface RedisService {
    <T> void set(String key, T value, Duration ttl);
    <T> T get(String key, Class<T> clazz);
    boolean exists(String key);
    void delete(String key);
}

