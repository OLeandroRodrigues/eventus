import java.time.Duration;

public interface RetryPolicy {
    boolean shouldRetry(int attempt, Exception exception);
    Duration nextDelay(int attempt);
}