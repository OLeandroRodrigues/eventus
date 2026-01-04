package io.github.oleandrorodrigues.eventus.transport.rabbitmq;

import io.github.oleandrorodrigues.eventus.bus.EventBus;
import io.github.oleandrorodrigues.eventus.event.Event;
import io.github.oleandrorodrigues.eventus.handler.EventHandler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class RabbitMqEventBus implements EventBus {

    private final RabbitMqPublisher publisher;

    // local registry (handlers live in-process)
    private final Map<String, List<EventHandler<? extends Event>>> handlers = new ConcurrentHashMap<>();

    public RabbitMqEventBus(RabbitMqPublisher publisher) {
        this.publisher = publisher;
    }

    /**
     * Publish to the broker (outgoing).
     * This should be used by application code that wants to emit events.
     */
    @Override
    public void publish(Event event) {
        try {
            publisher.publish(event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish event to RabbitMQ", e);
        }
    }

    @Override
    public void subscribe(String eventType, EventHandler<? extends Event> handler) {
        handlers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(handler);
    }

    /**
     * Dispatch to local handlers only (incoming).
     * This must be used by consumers to avoid republishing the same message.
     */
    public void dispatchLocal(Event event) throws Exception {
        List<EventHandler<? extends Event>> hs = handlers.getOrDefault(event.eventType(), List.of());
        for (EventHandler<? extends Event> h : hs) {
            @SuppressWarnings("unchecked")
            EventHandler<Event> typed = (EventHandler<Event>) h;
            typed.handle(event);
        }
    }
}
