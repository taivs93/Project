package com.taivs.project.service.auth.caching;

import com.taivs.project.entity.Session;
import com.taivs.project.entity.User;
import com.taivs.project.service.redis.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class AuthCachingServiceImpl implements AuthCachingService{

    @Autowired
    private RedisService redisService;

    @Override
    public void saveSession(Session session, long durationMs) {
        String key = "session:"+session.getId();
        redisService.set(key,session.getUser().getId(), Duration.ofMillis(durationMs));
    }

    @Override
    public Long getUserIdFromSession(String sessionId) {
        return redisService.get(sessionId, Long.class);
    }

    @Override
    public boolean isSessionExist(String sessionId) {
        return redisService.exists(sessionId);
    }

    @Override
    public void deleteSession(String sessionId) {
        redisService.delete(sessionId);
    }
}
