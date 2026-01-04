package io.github.oleandrorodrigues.eventus.transport.rabbitmq;

import io.github.oleandrorodrigues.eventus.event.EventMetadata;

import java.time.Instant;
import java.util.Map;

public record RabbitMqEventEnvelope(
        String eventId,
        String eventType,
        Instant occurredAt,
        Map<String, Object> payload,
        EventMetadata metadata
) {}
