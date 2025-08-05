package com.taivs.project.repository;

import com.taivs.project.entity.Session;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    @Query("""
        SELECT s FROM Session s
        WHERE s.id = :sessionId
        """)
    Optional<Session> findSessionById(@Param("sessionId") String sessionId);

    @Modifying
    @Transactional
    @Query("""
    DELETE FROM Session s WHERE s.user.id = :userId
    """)
    void deleteAllByUserId(@Param("userId") Long userId);

    @Query(value = """
    SELECT * FROM sessions s 
    WHERE s.user_id = :userId 
      AND s.expires_at > NOW()
    LIMIT 1
""", nativeQuery = true)
    Optional<Session> findActiveSessionByUserId(@Param("userId") Long userId);

}
