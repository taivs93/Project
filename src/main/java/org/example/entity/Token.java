package org.example.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Token extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", nullable = false, unique = true, length = 500)
    private String token;

    @Column(name = "expired", nullable = false)
    private boolean expired;

    @Column(name = "revoked", nullable = false)
    private boolean revoked;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(name = "token_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TokenType tokenType;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
