package com.expenlytics.core.usecase;

import static com.expenlytics.core.exception.AccountException.Reason.*;
import static org.junit.jupiter.api.Assertions.*;

import com.expenlytics.core.dao.AccountDAO;
import com.expenlytics.core.exception.AccountException;
import com.expenlytics.core.model.*;
import com.expenlytics.core.security.PasswordHasher;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class AccountServiceTest {

    private final MemoryAccounts dao = new MemoryAccounts();
    private final PasswordHasher passwords = new PasswordHasher();
    private final AccountService service = new AccountService(
        dao,
        passwords
    );
    private final AccountCredentials input = new AccountCredentials(
        " Jane ",
        " Jane@Example.com ",
        "long-password-123"
    );
    private final DeviceInfo device = new DeviceInfo(
        "127.0.0.1",
        null,
        "UNKNOWN",
        null,
        null,
        null,
        "SIGNUP"
    );

    @Test
    void acceptsEightCharacterSignupPassword() {
        var credentials = new AccountCredentials(
            "Jane",
            "eight@example.com",
            "12345678"
        );
        var result = service.authenticate(credentials, true, device);
        assertEquals("eight@example.com", result.profile().email());
    }

    @Test
    void rejectsSevenCharacterSignupPassword() {
        var credentials = new AccountCredentials(
            "Jane",
            "short@example.com",
            "1234567"
        );
        assertThrows(AccountException.class, () ->
            service.authenticate(credentials, true, device)
        );
    }

    @Test
    void signupHashesPasswordNormalizesEmailAndLinksEvent() {
        var result = service.authenticate(input, true, device);
        assertEquals("Jane", result.profile().name());
        assertEquals("jane@example.com", result.profile().email());
        assertTrue(
            passwords.matches(input.password(), dao.user.passwordHash())
        );
        assertNotEquals(input.password(), dao.user.passwordHash());
        assertNotEquals(result.token(), dao.tokenHash);
        assertTrue(dao.expiresAt.isAfter(Instant.now()));
        assertEquals(1, dao.events);
        assertEquals(result.profile(), service.current(result.token()));
    }

    @Test
    void duplicateEmailDoesNotAddEvents() {
        service.authenticate(input, true, device);
        assertEquals(
            DUPLICATE_EMAIL,
            assertThrows(AccountException.class, () ->
                service.authenticate(input, true, device)
            ).reason()
        );
        assertEquals(1, dao.events);
    }

    @Test
    void badPasswordAndUnknownEmailDoNotAddEvents() {
        service.authenticate(input, true, device);
        assertEquals(
            UNAUTHORIZED,
            assertThrows(AccountException.class, () ->
                service.authenticate(
                    new AccountCredentials(null, input.email(), "wrong"),
                    false,
                    device
                )
            ).reason()
        );
        assertEquals(
            UNAUTHORIZED,
            assertThrows(AccountException.class, () ->
                service.authenticate(
                    new AccountCredentials(
                        null,
                        "nobody@example.com",
                        "wrong"
                    ),
                    false,
                    device
                )
            ).reason()
        );
        assertEquals(1, dao.events);
    }

    @Test
    void loginReturnsExistingProfileAndCreatesEvent() {
        var first = service.authenticate(input, true, device);
        var second = service.authenticate(input, false, device);
        assertEquals(first.profile(), second.profile());
        assertNotEquals(first.token(), second.token());
        assertEquals(2, dao.events);
    }

    @Test
    void expiredAndRevokedSessionsCannotAccessProfile() {
        var first = service.authenticate(input, true, device);
        dao.expiresAt = Instant.now().minusSeconds(1);
        assertThrows(AccountException.class, () ->
            service.current(first.token())
        );
        var login = service.authenticate(input, false, device);
        String token = login.token();
        service.logout(token);
        assertThrows(AccountException.class, () ->
            service.current(token)
        );
    }

    @Test
    void eightCharacterPasswordSupportsSignupAndLogin() {
        var credentials = new AccountCredentials(
            "Jane",
            "jane@example.com",
            "eight123"
        );
        var signup = service.authenticate(credentials, true, device);
        var login = service.authenticate(credentials, false, device);
        assertEquals(signup.profile(), login.profile());
        assertTrue(
            passwords.matches("eight123", dao.user.passwordHash())
        );
        assertEquals(2, dao.events);
    }

    @Test
    void shortPasswordDoesNotCreateAnAccount() {
        assertEquals(
            INVALID_INPUT,
            assertThrows(AccountException.class, () ->
                service.authenticate(
                    new AccountCredentials(
                        "Jane",
                        "jane@example.com",
                        "1234567"
                    ),
                    true,
                    device
                )
            ).reason()
        );
        assertNull(dao.user);
        assertEquals(0, dao.events);
    }

    private static class MemoryAccounts implements AccountDAO {

        StoredAccount user;
        String tokenHash;
        Instant expiresAt;
        int events;

        public Optional<StoredAccount> findByEmail(String email) {
            return Optional.ofNullable(user).filter(u ->
                u.profile().email().equals(email)
            );
        }

        public AccountProfile register(
            String name,
            String email,
            String passwordHash,
            DeviceInfo device,
            String tokenHash,
            Instant expiresAt
        ) {
            user = new StoredAccount(
                new AccountProfile(42L, name, email, Instant.now()),
                passwordHash
            );
            recordLogin(42L, device, tokenHash, expiresAt);
            return user.profile();
        }

        public void recordLogin(
            Long userId,
            DeviceInfo device,
            String tokenHash,
            Instant expiresAt
        ) {
            assertEquals(user.profile().userId(), userId);
            this.tokenHash = tokenHash;
            this.expiresAt = expiresAt;
            events++;
        }

        public Optional<AccountProfile> findProfileBySession(
            String hash,
            Instant now
        ) {
            return hash.equals(tokenHash) && expiresAt.isAfter(now)
                ? Optional.of(user.profile())
                : Optional.empty();
        }

        public void deleteSession(String hash) {
            if (hash.equals(tokenHash)) tokenHash = null;
        }
    }
}
