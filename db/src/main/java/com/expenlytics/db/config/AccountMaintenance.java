package com.expenlytics.db.config;

import com.expenlytics.db.repository.UserDeviceInfoRepository;
import com.expenlytics.db.repository.UserSessionRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@EnableScheduling
public class AccountMaintenance {

    private final UserSessionRepository sessions;
    private final UserDeviceInfoRepository devices;
    private final int batchSize;
    private final int retentionDays;

    public AccountMaintenance(
        UserSessionRepository sessions,
        UserDeviceInfoRepository devices,
        @Value("${app.accounts.cleanup-batch-size:1000}") int batchSize,
        @Value(
            "${app.accounts.device-retention-days:0}"
        ) int retentionDays
    ) {
        if (
            batchSize < 1 || batchSize > 1000 || retentionDays < 0
        ) throw new IllegalArgumentException(
            "Invalid account cleanup configuration"
        );
        this.sessions = sessions;
        this.devices = devices;
        this.batchSize = batchSize;
        this.retentionDays = retentionDays;
    }

    @Scheduled(
        fixedDelayString = "${app.accounts.cleanup-delay-ms:60000}"
    )
    @Transactional
    public void cleanup() {
        var page = PageRequest.of(0, batchSize);
        var expired = sessions.findExpiredIds(Instant.now(), page);
        for (String hash : expired)
            sessions.endSession(hash, Instant.now());
        // Device-history deletion is opt-in; expiration of sessions is
        // always enforced on reads.
        if (retentionDays > 0) {
            var old = devices.findOldIds(
                Instant.now().minus(retentionDays, ChronoUnit.DAYS),
                page
            );
            if (!old.isEmpty()) devices.deleteAllByIdInBatch(old);
        }
    }
}
