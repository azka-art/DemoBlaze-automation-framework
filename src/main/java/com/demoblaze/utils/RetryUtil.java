package com.demoblaze.utils;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class RetryUtil {
    
    public static <T> T retry(Callable<T> action, Predicate<T> successCondition, 
                             Duration timeout, Duration pollInterval) {
        long startTime = System.currentTimeMillis();
        long timeoutMillis = timeout.toMillis();
        long pollMillis = pollInterval.toMillis();
        
        Exception lastException = null;
        
        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            try {
                T result = action.call();
                if (successCondition.test(result)) {
                    return result;
                }
            } catch (Exception e) {
                lastException = e;
            }
            
            try {
                TimeUnit.MILLISECONDS.sleep(pollMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while polling", e);
            }
        }
        
        throw new RuntimeException("Timeout after " + timeout + " waiting for condition", lastException);
    }
    
    public static void retryVoid(Runnable action, Duration timeout, Duration pollInterval) {
        retry(() -> {
            action.run();
            return true;
        }, result -> result, timeout, pollInterval);
    }
}