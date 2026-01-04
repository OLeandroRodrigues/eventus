package io.github.oleandrorodrigues.eventus.transport.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class RabbitMqEventSerializer {

    private final ObjectMapper mapper;

    public RabbitMqEventSerializer() {
        this.mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public byte[] serialize(RabbitMqEventEnvelope envelope) {
        try {
            return mapper.writeValueAsBytes(envelope);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize event envelope", e);
        }
    }
}
