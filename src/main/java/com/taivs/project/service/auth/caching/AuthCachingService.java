package com.taivs.project.service.auth.caching;

import com.taivs.project.entity.Session;

public interface AuthCachingService {

    void saveSession(Session session, long durationMs);

    String getTelFromSession(String sessionId);

    boolean isSessionExist(String sessionId);

    void deleteSession(String sessionId);
}
