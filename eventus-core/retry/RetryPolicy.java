package io.github.oleandrorodrigues.eventus.retry;

import java.time.Duration;

public interface RetryPolicy {
    boolean shouldRetry(int attempt, Exception exception);
    Duration nextDelay(int attempt);
}