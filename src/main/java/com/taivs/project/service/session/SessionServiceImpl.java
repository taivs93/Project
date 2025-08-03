package com.taivs.project.service.session;

import com.taivs.project.entity.Session;
import com.taivs.project.entity.User;
import com.taivs.project.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SessionServiceImpl implements SessionService{

    @Value("${jwt.session-expiration-ms}")
    private long expirationMs;

    @Autowired
    private SessionRepository sessionRepository;

    public Session createSession(User user) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusSeconds(this.expirationMs / 1000);
        Session session = Session.builder()
                .user(user)
                .createdAt(now)
                .expiresAt(expiresAt)
                .build();

        return sessionRepository.save(session);
    }
}
