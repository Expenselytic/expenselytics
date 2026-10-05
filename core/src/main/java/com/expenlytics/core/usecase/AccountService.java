package com.expenlytics.core.usecase;

import static com.expenlytics.core.exception.AccountException.Reason.*;

import com.expenlytics.core.dao.AccountDAO;
import com.expenlytics.core.exception.AccountException;
import com.expenlytics.core.model.*;
import com.expenlytics.core.security.PasswordHasher;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

    private final AccountDAO accounts;
    private final PasswordHasher passwords;
    private final String dummyHash;

    public AccountService(AccountDAO accounts, PasswordHasher passwords) {
        this.accounts = accounts;
        this.passwords = passwords;
        this.dummyHash = passwords.hash(UUID.randomUUID().toString());
    }

    public record LoginResult(String token, AccountProfile profile) {}

    // Hashing runs before the short database write transaction to avoid
    // holding a connection.
    public LoginResult authenticate(
        AccountCredentials input,
        boolean signup,
        DeviceInfo device
    ) {
        if (
            input == null ||
            input.email() == null ||
            input.password() == null
        ) throw invalid();
        String email = input.email().trim().toLowerCase(Locale.ROOT);
        if (
            email.length() > 254 ||
            !email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+") ||
            input.password().length() > 256
        ) throw invalid();
        var existing = accounts.findByEmail(email);
        String passwordHash = null;
        if (signup) {
            if (
                input.name() == null ||
                input.name().isBlank() ||
                input.name().trim().length() > 100 ||
                input.password().length() < 8
            ) throw invalid();
            if (existing.isPresent()) throw new AccountException(
                DUPLICATE_EMAIL,
                "An account with this email already exists"
            );
            passwordHash = passwords.hash(input.password());
        } else {
            boolean matches = passwords.matches(
                input.password(),
                existing
                    .map(StoredAccount::passwordHash)
                    .orElse(dummyHash)
            );
            if (
                existing.isEmpty() || !matches
            ) throw new AccountException(
                UNAUTHORIZED,
                "Invalid email or password"
            );
        }
        byte[] random = new byte[32];
        new SecureRandom().nextBytes(random);
        String token = Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(random);
        Instant expiresAt = Instant.now().plus(7, ChronoUnit.DAYS);
        AccountProfile profile;
        if (signup) {
            profile = accounts.register(
                input.name().trim(),
                email,
                passwordHash,
                device,
                digest(token),
                expiresAt
            );
        } else {
            profile = existing.orElseThrow().profile();
            accounts.recordLogin(
                profile.userId(),
                device,
                digest(token),
                expiresAt
            );
        }
        return new LoginResult(token, profile);
    }

    public AccountProfile current(String token) {
        return accounts
            .findProfileBySession(digest(token), Instant.now())
            .orElseThrow(() ->
                new AccountException(UNAUTHORIZED, "Please log in")
            );
    }

    public void logout(String token) {
        accounts.deleteSession(digest(token));
    }

    private AccountException invalid() {
        return new AccountException(
            INVALID_INPUT,
            "Enter a valid name, email, and password (8–256 " +
                "characters for signup)"
        );
    }

    private String digest(String token) {
        try {
            return HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(
                    token.getBytes(StandardCharsets.UTF_8)
                )
            );
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
