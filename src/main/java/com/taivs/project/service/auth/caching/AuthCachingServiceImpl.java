package com.taivs.project.service.auth.caching;

import com.taivs.project.entity.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.Objects;

@Service
public class AuthCachingServiceImpl implements AuthCachingService{

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void saveSession(Session session, long durationMs) {
        String key = "session:" + session.getId();
        redisTemplate.opsForValue().set(key,session.getUser().getTel());
    }

    @Override
    public String getTelFromSession(String sessionId) {
        String key = "session:" + sessionId;
        return Objects.requireNonNull(redisTemplate.opsForValue().get(key)).toString();
    }

    @Override
    public boolean isSessionExist(String sessionId) {
        return redisTemplate.hasKey("session:" + sessionId);
    }

    @Override
    public void deleteSession(String sessionId) {
        redisTemplate.delete("session:" + sessionId);
    }

}
