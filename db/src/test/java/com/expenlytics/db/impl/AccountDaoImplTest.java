package com.expenlytics.db.impl;

import static org.junit.jupiter.api.Assertions.*;

import com.expenlytics.core.model.DeviceInfo;
import com.expenlytics.db.config.AccountMaintenance;
import com.expenlytics.db.entity.UserEntity;
import com.expenlytics.db.repository.*;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.config.
    EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.TestTransaction;

@DataJpaTest(showSql = false)
@ContextConfiguration(classes = AccountDaoImplTest.Config.class)
class AccountDaoImplTest {

    @Configuration
    @EntityScan(basePackageClasses = UserEntity.class)
    @EnableJpaRepositories(basePackageClasses = UserRepository.class)
    @Import({ AccountDaoImpl.class, SpendingExperimentService.class })
    static class Config {}

    @Autowired
    AccountDaoImpl dao;

    @Autowired
    UserRepository users;

    @Autowired
    UserDeviceInfoRepository devices;

    @Autowired
    UserSessionRepository sessions;

    @Autowired
    SavingRepository savings;

    @Autowired
    ExpenseRepository expenses;

    @Test
    void savingsPersistSeparatelyFromExpenses() {
        long expenseCount = expenses.count();
        var saving = new com.expenlytics.db.entity.SavingEntity(
            new com.expenlytics.core.model.Expense(
                0,
                "Emergency fund",
                "Savings",
                "150.25",
                java.time.LocalDateTime.now().minusMinutes(1)
            )
        );
        var saved = savings.saveAndFlush(saving);
        var loaded = savings.findById(saved.getId()).orElseThrow();
        assertEquals("Emergency fund", loaded.getName());
        assertEquals("150.25", loaded.getAmount());
        assertEquals(expenseCount, expenses.count());
    }

    @Autowired
    SpendingExperimentService experiments;

    @Autowired
    jakarta.persistence.EntityManager entityManager;

    @Test
    void persistsLinksAndRemovesDeletedDeposits() {
        var request = new SpendingExperimentService.Request(
            "Cook more",
            "Food",
            java.time.LocalDate.now().minusDays(7),
            14
        );
        var first = experiments.create(request);
        var second = experiments.create(request);
        var saving = savings.saveAndFlush(
            new com.expenlytics.db.entity.SavingEntity(
                new com.expenlytics.core.model.Expense(
                    0,
                    "Fund",
                    "Savings",
                    "25",
                    java.time.LocalDateTime.now()
                )
            )
        );
        experiments.link(first.id(), saving.getId());
        entityManager.flush();
        entityManager.clear();
        assertEquals(
            java.util.List.of(saving.getId()),
            experiments
                .list()
                .stream()
                .filter(e -> e.id().equals(first.id()))
                .findFirst()
                .orElseThrow()
                .savingIds()
        );
        experiments.link(first.id(), saving.getId());
        experiments.unlink(first.id(), saving.getId());
        entityManager.flush();
        experiments.link(second.id(), saving.getId());
        entityManager.flush();
        entityManager.clear();
        savings.deleteById(saving.getId());
        savings.flush();
        entityManager.clear();
        assertTrue(
            experiments
                .list()
                .stream()
                .allMatch(e -> e.savingIds().isEmpty())
        );
    }

    @Test
    void rejectsInvalidExperiments() {
        assertThrows(IllegalArgumentException.class, () ->
            experiments.create(
                new SpendingExperimentService.Request(
                    "",
                    "Food",
                    java.time.LocalDate.now(),
                    0
                )
            )
        );
    }

    @Test
    void rejectsAttributingOneDepositToTwoExperiments() {
        var request = new SpendingExperimentService.Request(
            "Cook more",
            "Food",
            java.time.LocalDate.now().minusDays(7),
            14
        );
        var first = experiments.create(request);
        var second = experiments.create(request);
        var saving = savings.saveAndFlush(
            new com.expenlytics.db.entity.SavingEntity(
                new com.expenlytics.core.model.Expense(
                    0,
                    "Fund",
                    "Savings",
                    "25",
                    java.time.LocalDateTime.now()
                )
            )
        );
        experiments.link(first.id(), saving.getId());
        entityManager.flush();
        entityManager.clear();
        assertThrows(IllegalArgumentException.class, () ->
            experiments.link(second.id(), saving.getId())
        );
    }

    @Test
    void deletingExperimentPreservesDepositsAndAllowsRelinking() {
        var request = new SpendingExperimentService.Request(
            "Cook more",
            "Food",
            java.time.LocalDate.now().minusDays(7),
            14
        );
        var first = experiments.create(request);
        var second = experiments.create(request);
        var saving = savings.saveAndFlush(
            new com.expenlytics.db.entity.SavingEntity(
                new com.expenlytics.core.model.Expense(
                    0,
                    "Fund",
                    "Savings",
                    "25",
                    java.time.LocalDateTime.now()
                )
            )
        );
        experiments.link(first.id(), saving.getId());
        entityManager.flush();
        entityManager.clear();
        experiments.delete(first.id());
        entityManager.flush();
        entityManager.clear();
        assertEquals(1, experiments.list().size());
        assertEquals(second.id(), experiments.list().get(0).id());
        assertEquals(
            "25",
            savings.findById(saving.getId()).orElseThrow().getAmount()
        );
        experiments.delete(first.id());
        experiments.link(second.id(), saving.getId());
        entityManager.flush();
        entityManager.clear();
        assertEquals(
            java.util.List.of(saving.getId()),
            experiments.list().get(0).savingIds()
        );
    }

    private final DeviceInfo device = new DeviceInfo(
        "127.0.0.1",
        "Mozilla iPhone",
        "MOBILE",
        true,
        null,
        null,
        "SIGNUP"
    );

    @Test
    void persistsLinkedProfileEventAndSessionAndFindsProfileInOneJoin() {
        var profile = dao.register(
            "Jane",
            "jane@example.com",
            "hashed-password",
            device,
            "a".repeat(64),
            Instant.now().plusSeconds(300)
        );
        assertEquals(
            profile,
            dao
                .findProfileBySession("a".repeat(64), Instant.now())
                .orElseThrow()
        );
        assertEquals(profile.userId(), devices.findAll().get(0).user.id);
        assertEquals(1, devices.count());
        assertEquals(
            "hashed-password",
            dao
                .findByEmail("jane@example.com")
                .orElseThrow()
                .passwordHash()
        );
        assertTrue(
            dao
                .findProfileBySession(
                    "a".repeat(64),
                    Instant.now().plusSeconds(400)
                )
                .isEmpty()
        );
        dao.deleteSession("a".repeat(64));
        assertTrue(
            dao
                .findProfileBySession("a".repeat(64), Instant.now())
                .isEmpty()
        );
    }

    @Test
    void logoutRecordsTimeOnlyOnce() {
        var before = Instant.now();
        var profile = dao.register(
            "Jane",
            "logout@example.com",
            "hash",
            device,
            "e".repeat(64),
            before.plusSeconds(600)
        );
        assertNull(
            users.findById(profile.userId()).orElseThrow().lastLogin
        );
        dao.deleteSession("e".repeat(64));
        var ended = users
            .findById(profile.userId())
            .orElseThrow()
            .lastLogin;
        assertNotNull(ended);
        assertFalse(ended.isBefore(before));
        assertFalse(ended.isAfter(Instant.now()));
        dao.deleteSession("e".repeat(64));
        assertEquals(
            ended,
            users.findById(profile.userId()).orElseThrow().lastLogin
        );
    }

    @Test
    void recordsExpiryWithoutMovingTimestampBackwards() {
        var expiry = Instant.now()
            .minusSeconds(100)
            .truncatedTo(java.time.temporal.ChronoUnit.MICROS);
        var profile = dao.register(
            "Jane",
            "expiry@example.com",
            "hash",
            device,
            "e".repeat(64),
            expiry
        );
        dao.deleteSession("e".repeat(64));
        assertEquals(
            expiry,
            users.findById(profile.userId()).orElseThrow().lastLogin
        );
        dao.recordLogin(
            profile.userId(),
            device,
            "f".repeat(64),
            expiry.minusSeconds(50)
        );
        new AccountMaintenance(sessions, devices, 100, 0).cleanup();
        assertEquals(
            expiry,
            users.findById(profile.userId()).orElseThrow().lastLogin
        );
        assertEquals(0, sessions.count());
    }

    @Test
    void databaseRejectsDuplicateEmail() {
        dao.register(
            "Jane",
            "jane@example.com",
            "hash",
            device,
            "a".repeat(64),
            Instant.now()
        );
        assertThrows(DataIntegrityViolationException.class, () ->
            dao.register(
                "Other",
                "jane@example.com",
                "hash",
                device,
                "b".repeat(64),
                Instant.now()
            )
        );
    }

    @Test
    void cleanupIsBoundedAndPreservesActiveSessionsAndDeviceHistory() {
        var profile = dao.register(
            "Jane",
            "jane@example.com",
            "hash",
            device,
            "a".repeat(64),
            Instant.now().minusSeconds(60)
        );
        dao.recordLogin(
            profile.userId(),
            device,
            "b".repeat(64),
            Instant.now().minusSeconds(30)
        );
        dao.recordLogin(
            profile.userId(),
            device,
            "c".repeat(64),
            Instant.now().plusSeconds(600)
        );
        var cleanup = new AccountMaintenance(sessions, devices, 1, 0);
        cleanup.cleanup();
        assertEquals(2, sessions.count());
        assertEquals(3, devices.count());
        cleanup.cleanup();
        assertEquals(1, sessions.count());
        assertTrue(
            dao
                .findProfileBySession("c".repeat(64), Instant.now())
                .isPresent()
        );
    }

    @Test
    void registrationRollsBackWhenDeviceInsertFails() {
        // End the test transaction so the adapter owns the transaction
        // being checked.
        TestTransaction.end();
        long before = users.count();
        var invalidDevice = new DeviceInfo(
            null,
            null,
            null,
            null,
            null,
            null,
            "SIGNUP"
        );
        assertThrows(DataIntegrityViolationException.class, () ->
            dao.register(
                "Jane",
                "rollback@example.com",
                "hash",
                invalidDevice,
                "d".repeat(64),
                Instant.now().plusSeconds(60)
            )
        );
        assertEquals(before, users.count());
        assertTrue(users.findByEmail("rollback@example.com").isEmpty());
        assertFalse(sessions.existsById("d".repeat(64)));
    }
}
