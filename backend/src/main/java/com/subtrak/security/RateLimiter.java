package com.subtrak.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimiter {

    private final Map<String, Bucket> authBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> apiBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> dashboardBuckets = new ConcurrentHashMap<>();

    public boolean tryConsumeAuth(String ip) {
        return authBuckets.computeIfAbsent(ip, this::newAuthBucket).tryConsume(1);
    }

    public boolean tryConsumeApi(String key) {
        return apiBuckets.computeIfAbsent(key, this::newApiBucket).tryConsume(1);
    }

    public boolean tryConsumeDashboard(String ip) {
        return dashboardBuckets.computeIfAbsent(ip, this::newDashboardBucket).tryConsume(1);
    }

    public boolean tryConsume(String ip) {
        return tryConsumeAuth(ip);
    }

    private Bucket newAuthBucket(String key) {
        Bandwidth limit = Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket newApiBucket(String key) {
        Bandwidth limit = Bandwidth.classic(60, Refill.intervally(60, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket newDashboardBucket(String key) {
        Bandwidth limit = Bandwidth.classic(30, Refill.intervally(30, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }
}
