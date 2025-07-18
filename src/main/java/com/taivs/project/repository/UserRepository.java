package com.taivs.project.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.taivs.project.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = {
            "userRoles",
            "userRoles.role",
            "userRoles.role.rolePermissions",
            "userRoles.role.rolePermissions.permission"
    })
    @Query("""
            SELECT u FROM User u 
            WHERE u.tel = :tel
            AND u.status = 1
            """)
    Optional<User> findByTel(@Param("tel") String tel);
    boolean existsByTel(String tel);
    boolean existsByName(String name);

    @Query("""
            SELECT u FROM User u 
            """)
    Page<User> findAllUsers(Pageable pageable);
}

