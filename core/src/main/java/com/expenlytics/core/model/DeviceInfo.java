package com.expenlytics.core.model;

public record DeviceInfo(
    String ipAddress,
    String userAgent,
    String deviceType,
    Boolean mobile,
    String state,
    String country,
    String eventType
) {}
