package io.github.oleandrorodrigues.eventus.transport.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import io.github.oleandrorodrigues.eventus.event.Event;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class RabbitMqPublisher {

    public static final String HDR_ATTEMPT = "x-attempt";
    public static final String HDR_EVENT_TYPE = "x-event-type";

    private final Channel channel;
    private final RabbitMqTransportConfig cfg;
    private final RabbitMqEventSerializer serializer;

    public RabbitMqPublisher(Channel channel, RabbitMqTransportConfig cfg, RabbitMqEventSerializer serializer) {
        this.channel = channel;
        this.cfg = cfg;
        this.serializer = serializer;
    }

    public void publish(Event event) throws IOException {
        publishEnvelope(toEnvelope(event), 0, cfg.exchange(), event.eventType());
    }

    public void publishRetry(RabbitMqEventEnvelope envelope, int attempt, String retryQueueName) throws IOException {
        // Publish directly to retry queue via default exchange
        publishEnvelope(envelope, attempt, "", retryQueueName);
    }

    private void publishEnvelope(RabbitMqEventEnvelope envelope, int attempt, String exchange, String routingKey) throws IOException {
        byte[] body = serializer.serialize(envelope);

        Map<String, Object> headers = new HashMap<>();
        headers.put(HDR_ATTEMPT, attempt);
        headers.put(HDR_EVENT_TYPE, envelope.eventType());

        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                .contentType("application/json")
                .deliveryMode(2) // persistent
                .messageId(envelope.eventId())
                .headers(headers)
                .build();

        channel.basicPublish(exchange, routingKey, props, body);
    }

    private RabbitMqEventEnvelope toEnvelope(Event event) {
        return new RabbitMqEventEnvelope(
                event.eventId(),
                event.eventType(),
                event.occurredAt(),
                event.payload(),
                event.metadata()
        );
    }
}
