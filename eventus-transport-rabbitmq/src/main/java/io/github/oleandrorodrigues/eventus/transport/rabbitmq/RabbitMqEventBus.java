package io.github.oleandrorodrigues.eventus.transport.rabbitmq;

import com.rabbitmq.client.Channel;
import io.github.oleandrorodrigues.eventus.bus.EventBus;
import io.github.oleandrorodrigues.eventus.event.Event;
import io.github.oleandrorodrigues.eventus.handler.EventHandler;

import java.io.IOException;

public class RabbitMqEventBus implements EventBus {

    private final RabbitMqPublisher publisher;

    public RabbitMqEventBus(RabbitMqPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void publish(Event event) {
        try {
            publisher.publish(event);
        } catch (IOException e) {
            throw new RuntimeException("Failed to publish event", e);
        }
    }

    @Override
    public void subscribe(String eventType, EventHandler<? extends Event> handler) {
        // No-op here.
        // Subscriptions are handled by RabbitMqConsumer + local dispatch
    }
}
