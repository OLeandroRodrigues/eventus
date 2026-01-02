package io.github.oleandrorodrigues.eventus.examples;

import io.github.oleandrorodrigues.eventus.dlq.DeadLetterPublisher;
import io.github.oleandrorodrigues.eventus.event.Event;
import io.github.oleandrorodrigues.eventus.event.EventMetadata;
import io.github.oleandrorodrigues.eventus.retry.RetryPolicy;
import io.github.oleandrorodrigues.eventus.transport.inmemory.InMemoryEventBus;

import java.time.Instant;
import java.time.Duration;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        RetryPolicy retryPolicy = new RetryPolicy() {
            @Override
            public boolean shouldRetry(int attempt, Exception exception) {
                return attempt < 3;
            }

            @Override
            public Duration nextDelay(int attempt) {
                return Duration.ofMillis(100);
            }
        };

        DeadLetterPublisher dlq = (event, ex) ->
            System.out.println("DLQ: " + event.eventType() + " reason=" + ex.getClass().getSimpleName());

        InMemoryEventBus bus = new InMemoryEventBus(retryPolicy, dlq);

        bus.subscribe("order.placed", e ->
            System.out.println("Handled: " + e.eventType() + " id=" + e.eventId())
        );

        Event evt = new Event() {
            @Override public String eventId() { return "1"; }
            @Override public String eventType() { return "order.placed"; }
            @Override public Instant occurredAt() { return Instant.now(); }
            @Override public Map<String, Object> payload() { return Map.of("orderId", 123); }
            @Override public EventMetadata metadata() { return new EventMetadata("c1","ca1","examples", Map.of()); }
        };

        bus.publish(evt);
    }
}