package com.expenlytics.db.impl;

import com.expenlytics.core.dao.AccountDAO;
import com.expenlytics.core.model.*;
import com.expenlytics.db.entity.*;
import com.expenlytics.db.repository.*;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class AccountDaoImpl implements AccountDAO {

    private final jakarta.persistence.EntityManager entityManager;
    private final UserRepository users;
    private final UserDeviceInfoRepository devices;
    private final UserSessionRepository sessions;

    public AccountDaoImpl(
        UserRepository users,
        UserDeviceInfoRepository devices,
        UserSessionRepository sessions,
        jakarta.persistence.EntityManager entityManager
    ) {
        this.entityManager = entityManager;
        this.users = users;
        this.devices = devices;
        this.sessions = sessions;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StoredAccount> findByEmail(String email) {
        return users
            .findByEmail(email)
            .map(user ->
                new StoredAccount(profile(user), user.passwordHash)
            );
    }

    @Override
    @Transactional
    public AccountProfile register(
        String name,
        String email,
        String passwordHash,
        DeviceInfo device,
        String tokenHash,
        Instant expiresAt
    ) {
        UserEntity user = new UserEntity();
        user.name = name;
        user.email = email;
        user.passwordHash = passwordHash;
        // Unique email constraint arbitrates concurrent signups.
        users.saveAndFlush(user);
        saveEvent(user, device, tokenHash, expiresAt);
        return profile(user);
    }

    @Override
    @Transactional
    public void recordLogin(
        Long userId,
        DeviceInfo device,
        String tokenHash,
        Instant expiresAt
    ) {
        saveEvent(
            users.getReferenceById(userId),
            device,
            tokenHash,
            expiresAt
        );
    }

    private void saveEvent(
        UserEntity user,
        DeviceInfo device,
        String tokenHash,
        Instant expiresAt
    ) {
        UserDeviceInfoEntity row = new UserDeviceInfoEntity();
        row.user = user;
        row.ipAddress = device.ipAddress();
        row.userAgent = device.userAgent();
        row.deviceType = device.deviceType();
        row.mobile = device.mobile();
        row.state = device.state();
        row.country = device.country();
        row.eventType = device.eventType();
        devices.save(row);
        UserSessionEntity session = new UserSessionEntity();
        session.tokenHash = tokenHash;
        session.user = user;
        session.expiresAt = expiresAt;
        // New random IDs: avoid merge's unnecessary existence query.
        entityManager.persist(session);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AccountProfile> findProfileBySession(
        String tokenHash,
        Instant now
    ) {
        return sessions.findProfile(tokenHash, now);
    }

    @Override
    @Transactional
    public void deleteSession(String tokenHash) {
        sessions.endSession(tokenHash, Instant.now());
    }

    private AccountProfile profile(UserEntity user) {
        return new AccountProfile(
            user.id,
            user.name,
            user.email,
            user.createdAt
        );
    }
}
