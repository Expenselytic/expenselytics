package com.expenlytics.db.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "`user`",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_user_email",
        columnNames = "email"
    )
)
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    public Long id;

    @Column(nullable = false, length = 100)
    public String name;

    @Column(nullable = false, length = 254)
    public String email;

    @Column(nullable = false, length = 512)
    public String passwordHash;

    @Column(name = "last_login")
    public Instant lastLogin;

    @Column(nullable = false)
    public Instant createdAt = Instant.now();
}
