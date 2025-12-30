public interface IdempotencyStore {
    boolean isProcessed(String eventId, String handlerName);
    void markProcessed(String eventId, String handlerName);
}