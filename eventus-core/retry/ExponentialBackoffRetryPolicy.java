package io.github.oleandrorodrigues.eventus.retry;

import java.time.Duration;
import java.util.Objects;
import java.util.Random;

public final class ExponentialBackoffRetryPolicy implements RetryPolicy {

    private final int maxAttempts;
    private final Duration initialDelay;
    private final Duration maxDelay;
    private final double multiplier;
    private final boolean jitterEnabled;

    private final Random random = new Random();

    public ExponentialBackoffRetryPolicy(
            int maxAttempts,
            Duration initialDelay,
            Duration maxDelay,
            double multiplier,
            boolean jitterEnabled
    ) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be >= 1");
        }
        this.maxAttempts = maxAttempts;
        this.initialDelay = Objects.requireNonNull(initialDelay);
        this.maxDelay = Objects.requireNonNull(maxDelay);
        this.multiplier = multiplier;
        this.jitterEnabled = jitterEnabled;
    }

    @Override
    public boolean shouldRetry(int attempt, Exception exception) {
        return attempt < maxAttempts;
    }

    @Override
    public Duration nextDelay(int attempt) {
        double exponential = initialDelay.toMillis()
                * Math.pow(multiplier, Math.max(0, attempt - 1));

        long delayMillis = (long) Math.min(exponential, maxDelay.toMillis());

        if (jitterEnabled) {
            delayMillis = applyJitter(delayMillis);
        }

        return Duration.ofMillis(delayMillis);
    }

    private long applyJitter(long delayMillis) {
        // Full jitter: random between 0 and delay
        return (long) (random.nextDouble() * delayMillis);
    }
}