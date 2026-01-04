package io.github.oleandrorodrigues.eventus.transport.rabbitmq;

import java.time.Duration;
import java.util.List;

public record RabbitMqTransportConfig(
        String exchange,              // e.g. "eventus.events"
        String deadLetterExchange,    // e.g. "eventus.dlx"
        String queue,                 // e.g. "eventus.examples"
        String dlq,                   // e.g. "eventus.examples.dlq"
        List<Duration> retryBackoff   // e.g. [1s, 10s, 60s]
) {
    public static RabbitMqTransportConfig defaults(String consumerGroup) {
        return new RabbitMqTransportConfig(
                "eventus.events",
                "eventus.dlx",
                "eventus." + consumerGroup,
                "eventus." + consumerGroup + ".dlq",
                List.of(Duration.ofSeconds(1), Duration.ofSeconds(10), Duration.ofSeconds(60))
        );
    }

    public String retryQueueName(Duration delay) {
        // eventus.<group>.retry.1000ms
        return queue() + ".retry." + delay.toMillis() + "ms";
    }
}
