package com.steve0v0.home.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class RateLimitService {

    @Value("${steve.rate-limit.max-attempts:5}")
    private int maxAttempts;

    @Value("${steve.rate-limit.window-minutes:5}")
    private int windowMinutes;

    private final Map<String, AttemptRecord> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String ip) {
        AttemptRecord record = attempts.get(ip);
        if (record == null) {
            return false;
        }
        if (System.currentTimeMillis() - record.windowStart > windowMinutes * 60_000L) {
            attempts.remove(ip);
            return false;
        }
        return record.count.get() >= maxAttempts;
    }

    public void recordFailure(String ip) {
        long now = System.currentTimeMillis();
        attempts.compute(ip, (key, record) -> {
            if (record == null || now - record.windowStart > windowMinutes * 60_000L) {
                return new AttemptRecord(now);
            }
            record.count.incrementAndGet();
            return record;
        });
    }

    public void recordSuccess(String ip) {
        attempts.remove(ip);
    }

    private static class AttemptRecord {
        final long windowStart;
        final AtomicInteger count;

        AttemptRecord(long windowStart) {
            this.windowStart = windowStart;
            this.count = new AtomicInteger(1);
        }
    }
}
