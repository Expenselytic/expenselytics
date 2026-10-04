package com.expenlytics.core.model;

public record StoredAccount(
    AccountProfile profile,
    String passwordHash
) {}
