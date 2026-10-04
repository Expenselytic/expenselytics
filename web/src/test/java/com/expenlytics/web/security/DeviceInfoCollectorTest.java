package com.expenlytics.web.security;

import static org.junit.jupiter.api.Assertions.*;

import java.net.InetSocketAddress;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.
    MockServerHttpRequest;

class DeviceInfoCollectorTest {

    @Test
    void ignoresSpoofedForwardingHeadersAndDetectsPhone()
        throws Exception {
        var collector = new DeviceInfoCollector("", "");
        var request = MockServerHttpRequest.post("/api/v1/auth/signup")
            .remoteAddress(new InetSocketAddress("127.0.0.1", 1234))
            .header("X-Forwarded-For", "8.8.8.8")
            .header("User-Agent", "Mozilla iPhone Mobile")
            .build();
        var info = collector.collect(request, "SIGNUP");
        assertEquals("127.0.0.1", info.ipAddress());
        assertEquals("MOBILE", info.deviceType());
        assertTrue(info.mobile());
        assertNull(info.state());
        assertNull(info.country());
    }

    @Test
    void walksTrustedProxyChainAndStopsBeforeSpoofedAddress()
        throws Exception {
        var collector = new DeviceInfoCollector("", "127.0.0.1,10.0.0.1");
        var request = MockServerHttpRequest.post("/api/v1/auth/login")
            .remoteAddress(new InetSocketAddress("127.0.0.1", 1234))
            .header("X-Forwarded-For", "1.1.1.1, 8.8.8.8, 10.0.0.1")
            .build();
        var info = collector.collect(request, "LOGIN");
        assertEquals("8.8.8.8", info.ipAddress());
        assertEquals("UNKNOWN", info.deviceType());
        assertNull(info.mobile());
    }

    @Test
    void rejectsNonAddressForwardedValues() throws Exception {
        var collector = new DeviceInfoCollector("", "127.0.0.1");
        var request = MockServerHttpRequest.post("/")
            .remoteAddress(new InetSocketAddress("127.0.0.1", 1))
            .header("X-Forwarded-For", "attacker.example.com")
            .header("User-Agent", "Mozilla iPad")
            .build();
        var info = collector.collect(request, "LOGIN");
        assertEquals("127.0.0.1", info.ipAddress());
        assertEquals("TABLET", info.deviceType());
    }
}
