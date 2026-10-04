package com.expenlytics.db.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "user_device_info",
    indexes = {
        @Index(
            name = "idx_device_user_created",
            columnList = "user_id, created_at"
        ),
        @Index(name = "idx_device_created", columnList = "created_at"),
    }
)
public class UserDeviceInfoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    public UserEntity user;

    @Column(length = 45)
    public String ipAddress;

    @Column(length = 1024)
    public String userAgent;

    @Column(nullable = false, length = 16)
    public String deviceType;

    public Boolean mobile;
    public String state;
    public String country;

    @Column(nullable = false, length = 16)
    public String eventType;

    @Column(nullable = false)
    public Instant createdAt = Instant.now();
}
