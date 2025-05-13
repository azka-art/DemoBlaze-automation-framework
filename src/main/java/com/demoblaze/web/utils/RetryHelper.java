package com.demoblaze.web.utils;

import java.time.Duration;
import java.util.function.Supplier;

public class RetryHelper {
    private static final int MAX_RETRIES = 3;
    private static final Duration WAIT_DURATION = Duration.ofSeconds(2);
    
    public static <T> T executeWithRetry(Supplier<T> action) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                return action.get();
            } catch (Exception e) {
                if (i == MAX_RETRIES - 1) throw e;
                try {
                    Thread.sleep(WAIT_DURATION.toMillis());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        throw new RuntimeException("Failed after " + MAX_RETRIES + " retries");
    }
}
