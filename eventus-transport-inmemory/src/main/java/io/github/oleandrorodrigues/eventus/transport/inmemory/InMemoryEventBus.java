package io.github.oleandrorodrigues.eventus.transport.inmemory;

import io.github.oleandrorodrigues.eventus.bus.EventBus;
import io.github.oleandrorodrigues.eventus.dlq.DeadLetterPublisher;
import io.github.oleandrorodrigues.eventus.event.Event;
import io.github.oleandrorodrigues.eventus.exception.RetryableException;
import io.github.oleandrorodrigues.eventus.handler.EventHandler;
import io.github.oleandrorodrigues.eventus.retry.RetryPolicy;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class InMemoryEventBus implements EventBus {

    private final Map<String, List<EventHandler<? extends Event>>> handlers = new ConcurrentHashMap<>();

    private final RetryPolicy retryPolicy;
    private final DeadLetterPublisher deadLetterPublisher;

    public InMemoryEventBus(RetryPolicy retryPolicy, DeadLetterPublisher deadLetterPublisher) {
        this.retryPolicy = retryPolicy;
        this.deadLetterPublisher = deadLetterPublisher;
    }

    @Override
    public void publish(Event event) {
        List<EventHandler<? extends Event>> eventHandlers =
                handlers.getOrDefault(event.eventType(), List.of());

        for (EventHandler<? extends Event> handler : eventHandlers) {
            dispatchWithRetry(event, handler);
        }
    }

    @Override
    public void subscribe(String eventType, EventHandler<? extends Event> handler) {
        handlers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(handler);
    }

    @SuppressWarnings("unchecked")
    private void dispatchWithRetry(Event event, EventHandler<? extends Event> rawHandler) {
        EventHandler<Event> handler = (EventHandler<Event>) rawHandler;

        int attempt = 0;

        while (true) {
            try {
                attempt++;
                handler.handle(event);
                return; // success
            } catch (RetryableException ex) {
                if (!retryPolicy.shouldRetry(attempt, ex)) {
                    deadLetterPublisher.publish(event, ex);
                    return;
                }
                sleep(retryPolicy.nextDelay(attempt));
            } catch (Exception ex) {
                deadLetterPublisher.publish(event, ex);
                return;
            }
        }
    }

    private void sleep(Duration delay) {
        try {
            Thread.sleep(delay.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}