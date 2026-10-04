package com.expenlytics.web.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Configuration
public class AccountExecutionConfig {

    @Bean(destroyMethod = "dispose")
    public Scheduler accountScheduler(
        @Value("${app.accounts.workers:4}") int workers,
        @Value("${app.accounts.queued-per-worker:25}") int queuedPerWorker
    ) {
        return Schedulers.newBoundedElastic(
            workers,
            queuedPerWorker,
            "account-auth"
        );
    }
}
