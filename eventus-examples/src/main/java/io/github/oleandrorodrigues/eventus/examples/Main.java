package io.github.oleandrorodrigues.eventus.examples;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.github.oleandrorodrigues.eventus.event.Event;
import io.github.oleandrorodrigues.eventus.event.EventMetadata;
import io.github.oleandrorodrigues.eventus.handler.EventHandler;
import io.github.oleandrorodrigues.eventus.retry.RetryPolicy;
import io.github.oleandrorodrigues.eventus.transport.rabbitmq.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        // 1) Rabbit connection
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");

        try (Connection conn = factory.newConnection("eventus-examples");
             Channel channel = conn.createChannel()) {

            // 2) Transport config (consumer group = "examples")
            RabbitMqTransportConfig cfg = RabbitMqTransportConfig.defaults("examples");

            // 3) Declare topology
            RabbitMqTopology.declare(channel, cfg, "demo.#");

            // 4) Serializer / deserializer / publisher
            RabbitMqEventSerializer serializer = new RabbitMqEventSerializer();
            RabbitMqEventDeserializer deserializer = new RabbitMqEventDeserializer();
            RabbitMqPublisher publisher = new RabbitMqPublisher(channel, cfg, serializer);

            // 5) Local bus for handler registry (IMPORTANT: keep it typed as RabbitMqEventBus)
            RabbitMqEventBus localBus = new RabbitMqEventBus(publisher);

            // 6) Retry policy (example)
            RetryPolicy retryPolicy = new RetryPolicy() {
                @Override
                public boolean shouldRetry(int attempt, Exception exception) {
                    // retry 3 times for any exception
                    return attempt <= 3;
                }

                @Override
                public Duration nextDelay(int attempt) {
                    return switch (attempt) {
                        case 1 -> Duration.ofSeconds(1);
                        case 2 -> Duration.ofSeconds(10);
                        default -> Duration.ofSeconds(60);
                    };
                }
            };

            // 7) Consumer wired to local dispatcher
            RabbitMqConsumer consumer = new RabbitMqConsumer(
                    channel, cfg, deserializer, publisher, localBus, retryPolicy
            );

            // 8) Subscribe handler
            localBus.subscribe("demo.hello", (EventHandler<Event>) event -> {
                log.info("HANDLER received: type={} id={} payload={}",
                        event.eventType(),
                        event.eventId(),
                        event.payload()
                );

                // Uncomment to test retries:
                // throw new RuntimeException("Simulated failure");
            });

            // 9) Start consumer
            consumer.start();

            // 10) Publish one event
            EventMetadata md = new EventMetadata("corr-1", "cause-1", "examples", Map.of("env", "local"));
            Event e = new SimpleEvent(
                    "evt-1",
                    "demo.hello",
                    Instant.now(),
                    Map.of("message", "Hello from Eventus + RabbitMQ"),
                    md
            );

            localBus.publish(e);

            log.info("Published demo event. Keep the app running to consume.");
            Thread.sleep(60_000);
        }
    }

    // Concrete Event used only by examples
    private record SimpleEvent(
            String eventId,
            String eventType,
            Instant occurredAt,
            Map<String, Object> payload,
            EventMetadata metadata
    ) implements Event {}
}