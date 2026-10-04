package com.expenlytics.web.security;

import com.expenlytics.core.model.DeviceInfo;
import com.maxmind.geoip2.DatabaseReader;
import jakarta.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class DeviceInfoCollector {

    private final DatabaseReader reader;
    private final Set<String> trustedProxies;

    public DeviceInfoCollector(
        @Value(
            "${app.geoip.database-path:${GEOIP_DATABASE_PATH:}}"
        ) String path,
        @Value(
            "${app.security.trusted-proxies:${APP_TRUSTED_PROXIES:}}"
        ) String proxies
    ) throws IOException {
        reader = path.isBlank()
            ? null
            : new DatabaseReader.Builder(new File(path))
                  .withCache(new com.maxmind.db.CHMCache())
                  .build();
        trustedProxies = Arrays.stream(proxies.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toSet());
    }

    public DeviceInfo collect(ServerHttpRequest request, String event) {
        var remote = request.getRemoteAddress();
        String ip =
            remote == null || remote.getAddress() == null
                ? null
                : remote.getAddress().getHostAddress();
        // Walk from the closest hop; never trust a client-supplied
        // leftmost address.
        String forwarded = request
            .getHeaders()
            .getFirst("X-Forwarded-For");
        if (
            ip != null && trustedProxies.contains(ip) && forwarded != null
        ) {
            String[] hops = forwarded.split(",");
            for (
                int i = hops.length - 1;
                i >= 0 && trustedProxies.contains(ip);
                i--
            ) {
                String candidate = hops[i].trim();
                if (!literal(candidate)) break;
                ip = candidate;
            }
        }
        String agent = request.getHeaders().getFirst("User-Agent");
        String userAgent =
            agent == null
                ? null
                : agent.substring(0, Math.min(agent.length(), 1024));
        String ua = agent == null ? "" : agent.toLowerCase(Locale.ROOT);
        boolean tablet =
            ua.contains("ipad") ||
            ua.contains("tablet") ||
            (ua.contains("android") && !ua.contains("mobile"));
        boolean phone =
            !tablet &&
            (ua.contains("mobi") ||
                ua.contains("iphone") ||
                "?1".equals(
                    request.getHeaders().getFirst("Sec-CH-UA-Mobile")
                ));
        String deviceType = tablet
            ? "TABLET"
            : phone
              ? "MOBILE"
              : ua.isBlank()
                ? "UNKNOWN"
                : "DESKTOP";
        Boolean mobile = ua.isBlank() && !phone ? null : phone;
        String state = null;
        String country = null;
        if (reader != null && ip != null) {
            try {
                var location = reader.city(InetAddress.getByName(ip));
                state = location.getMostSpecificSubdivision().getName();
                country = location.getCountry().getName();
            } catch (
                IOException
                | com.maxmind.geoip2.exception.GeoIp2Exception ignored
            ) {
                // Private/unmapped addresses have no location; signup
                // must still succeed.
            }
        }
        return new DeviceInfo(
            ip,
            userAgent,
            deviceType,
            mobile,
            state,
            country,
            event
        );
    }

    private boolean literal(String value) {
        if (value.length() > 45) return false;
        if (value.contains(":")) {
            if (!value.matches("[0-9a-fA-F:.]+")) return false;
        } else if (
            !value.matches("[0-9]{1,3}(\\.[0-9]{1,3}){3}")
        ) return false;
        try {
            InetAddress.getByName(value);
            return value.contains(":") || value.split("\\.").length == 4;
        } catch (IOException e) {
            return false;
        }
    }

    @PreDestroy
    public void close() throws IOException {
        if (reader != null) reader.close();
    }
}
