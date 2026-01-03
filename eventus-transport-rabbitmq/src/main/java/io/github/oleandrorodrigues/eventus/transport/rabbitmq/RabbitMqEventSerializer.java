package io.github.oleandrorodrigues.eventus.transport.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.oleandrorodrigues.eventus.event.Event;

public class RabbitMqEventSerializer {

    private final ObjectMapper mapper = new ObjectMapper();

    public byte[] serialize(Event event) {
        try {
            return mapper.writeValueAsBytes(event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize event", e);
        }
    }
}