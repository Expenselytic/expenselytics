package com.expenlytics.db.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "user_session",
    indexes = {
        @Index(name = "idx_session_expiry", columnList = "expires_at"),
        @Index(name = "idx_session_user", columnList = "user_id"),
    }
)
public class UserSessionEntity {

    @Id
    @Column(length = 64)
    public String tokenHash;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    public UserEntity user;

    @Column(nullable = false)
    public Instant expiresAt;
}
