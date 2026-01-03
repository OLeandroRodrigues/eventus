package io.github.oleandrorodrigues.eventus.transport.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.oleandrorodrigues.eventus.event.Event;

public class RabbitMqEventDeserializer {

    private final ObjectMapper mapper = new ObjectMapper();

    public Event deserialize(byte[] body) {
        try {
            return mapper.readValue(body, Event.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize event", e);
        }
    }
}