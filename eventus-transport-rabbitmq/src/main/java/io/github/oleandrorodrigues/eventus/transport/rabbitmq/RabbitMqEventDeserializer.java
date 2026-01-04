package io.github.oleandrorodrigues.eventus.transport.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class RabbitMqEventDeserializer {

    private final ObjectMapper mapper;

    public RabbitMqEventDeserializer() {
        this.mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public RabbitMqEventEnvelope deserialize(byte[] body) {
        try {
            return mapper.readValue(body, RabbitMqEventEnvelope.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize event envelope", e);
        }
    }
}