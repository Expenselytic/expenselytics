package com.expenlytics.core.dao;

import com.expenlytics.core.model.*;
import java.time.Instant;
import java.util.Optional;

/**
 * Atomic persistence boundaries; implementations must not perform
 * password hashing.
 */
public interface AccountDAO {
    Optional<StoredAccount> findByEmail(String email);
    AccountProfile register(
        String name,
        String email,
        String passwordHash,
        DeviceInfo device,
        String tokenHash,
        Instant expiresAt
    );
    void recordLogin(
        Long userId,
        DeviceInfo device,
        String tokenHash,
        Instant expiresAt
    );
    Optional<AccountProfile> findProfileBySession(
        String tokenHash,
        Instant now
    );
    void deleteSession(String tokenHash);
}
