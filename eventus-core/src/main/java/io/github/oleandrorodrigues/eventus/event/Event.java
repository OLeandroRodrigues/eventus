package io.github.oleandrorodrigues.eventus.event;

import java.time.Instant;
import java.util.Map;

public interface Event {
    String eventId();
    String eventType();
    Instant occurredAt();
    Map<String, Object> payload();
    EventMetadata metadata();
}
