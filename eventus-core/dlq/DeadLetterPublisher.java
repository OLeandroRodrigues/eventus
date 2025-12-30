public interface DeadLetterPublisher {
    void publish(Event event, Exception cause);
}