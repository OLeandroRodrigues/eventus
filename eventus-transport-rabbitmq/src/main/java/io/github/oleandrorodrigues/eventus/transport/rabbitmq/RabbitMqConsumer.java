package io.github.oleandrorodrigues.eventus.transport.rabbitmq;

import com.rabbitmq.client.*;
import io.github.oleandrorodrigues.eventus.bus.EventBus;
import io.github.oleandrorodrigues.eventus.event.Event;
import io.github.oleandrorodrigues.eventus.retry.RetryPolicy;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

public final class RabbitMqConsumer {

    private final Channel channel;
    private final RabbitMqTransportConfig cfg;
    private final RabbitMqEventDeserializer deserializer;
    private final RabbitMqPublisher publisher;
    private final RabbitMqEventBus eventBus;
    private final RetryPolicy retryPolicy;

    
    public RabbitMqConsumer(Channel channel,
                            RabbitMqTransportConfig cfg,
                            RabbitMqEventDeserializer deserializer,
                            RabbitMqPublisher publisher,
                            EventBus localDispatcher,
                            RetryPolicy retryPolicy) {
        this(
                channel,
                cfg,
                deserializer,
                publisher,
                (RabbitMqEventBus) localDispatcher, // safe cast (no seu caso)
                retryPolicy
        );
    }

    
    public RabbitMqConsumer(Channel channel,
                            RabbitMqTransportConfig cfg,
                            RabbitMqEventDeserializer deserializer,
                            RabbitMqPublisher publisher,
                            RabbitMqEventBus eventBus,
                            RetryPolicy retryPolicy) {
        this.channel = channel;
        this.cfg = cfg;
        this.deserializer = deserializer;
        this.publisher = publisher;
        this.eventBus = eventBus;
        this.retryPolicy = retryPolicy;
    }

   
    public void start() throws IOException {
        channel.basicQos(50);

        channel.basicConsume(cfg.queue(), false, new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String tag,
                                       Envelope env,
                                       AMQP.BasicProperties props,
                                       byte[] body) throws IOException {

                long deliveryTag = env.getDeliveryTag();
                RabbitMqEventEnvelope envelope = null;

                try {
                    envelope = deserializer.deserialize(body);
                    Event event = toEvent(envelope);

                    // DISPATCH LOCAL ONLY (NO REPUBLISH)
                    eventBus.dispatchLocal(event);

                    channel.basicAck(deliveryTag, false);
                } catch (Exception ex) {
                    int attempt = readAttempt(props);

                    boolean shouldRetry = retryPolicy.shouldRetry(attempt + 1, ex);

                    if (shouldRetry && envelope != null) {
                        Duration delay = retryPolicy.nextDelay(attempt + 1);
                        String retryQueue = pickRetryQueue(delay);

                        try {
                            publisher.publishRetry(envelope, attempt + 1, retryQueue);
                            channel.basicAck(deliveryTag, false);
                        } catch (Exception publishEx) {
                            // temporary failure → requeue original
                            channel.basicNack(deliveryTag, false, true);
                        }
                    } else {
                        // non-retryable or exceeded attempts → DLQ
                        channel.basicReject(deliveryTag, false);
                    }
                }
            }
        });
    }

    
    private int readAttempt(AMQP.BasicProperties props) {
        Map<String, Object> headers =
                Optional.ofNullable(props.getHeaders()).orElse(Map.of());

        Object raw = headers.get(RabbitMqPublisher.HDR_ATTEMPT);

        if (raw == null) return 0;
        if (raw instanceof Integer i) return i;
        if (raw instanceof Long l) return l.intValue();
        if (raw instanceof String s) {
            try {
                return Integer.parseInt(s);
            } catch (Exception ignored) {}
        }
        return 0;
    }

    private String pickRetryQueue(Duration desiredDelay) {
        Duration chosen = cfg.retryBackoff().get(0);

        for (Duration d : cfg.retryBackoff()) {
            if (d.toMillis() >= desiredDelay.toMillis()) {
                chosen = d;
                break;
            }
            chosen = d;
        }
        return cfg.retryQueueName(chosen);
    }

    private Event toEvent(RabbitMqEventEnvelope env) {
        return new TransportEvent(
                env.eventId(),
                env.eventType(),
                env.occurredAt(),
                env.payload(),
                env.metadata()
        );
    }

   
    private record TransportEvent(
            String eventId,
            String eventType,
            java.time.Instant occurredAt,
            java.util.Map<String, Object> payload,
            io.github.oleandrorodrigues.eventus.event.EventMetadata metadata
    ) implements Event {}
}
