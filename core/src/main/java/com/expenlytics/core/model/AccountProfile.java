package com.expenlytics.core.model;

import java.time.Instant;

public record AccountProfile(
    Long userId,
    String name,
    String email,
    Instant createdAt
) {}
