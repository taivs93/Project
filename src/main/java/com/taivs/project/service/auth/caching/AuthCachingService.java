package com.taivs.project.service.auth.caching;

import com.taivs.project.entity.Session;

public interface AuthCachingService {

    void saveSession(Session session, long durationMs);

    Long getUserIdFromSession(String sessionId);

    boolean isSessionExist(String sessionId);

    void deleteSession(String sessionId);
}
