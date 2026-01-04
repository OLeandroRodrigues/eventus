package io.github.oleandrorodrigues.eventus.transport.rabbitmq;

import com.rabbitmq.client.Channel;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public final class RabbitMqTopology {

    private RabbitMqTopology() {}

    public static void declare(Channel channel, RabbitMqTransportConfig cfg, String... bindingKeys) throws IOException {
        // Exchanges
        channel.exchangeDeclare(cfg.exchange(), "topic", true);
        channel.exchangeDeclare(cfg.deadLetterExchange(), "topic", true);

        // DLQ (bound to DLX with routing key = queue name)
        channel.queueDeclare(cfg.dlq(), true, false, false, Map.of());
        channel.queueBind(cfg.dlq(), cfg.deadLetterExchange(), cfg.queue());

        // Main queue with DLX configured
        Map<String, Object> mainArgs = new HashMap<>();
        mainArgs.put("x-dead-letter-exchange", cfg.deadLetterExchange());
        // When rejected, we route into DLX using routing key = main queue name via basicReject(requeue=false)
        // We'll use cfg.queue() as routing key in our consumer when rejecting.
        channel.queueDeclare(cfg.queue(), true, false, false, mainArgs);

        // Bindings (consumer group queue consumes specific event types/patterns)
        if (bindingKeys == null || bindingKeys.length == 0) {
            // Default: bind everything
            channel.queueBind(cfg.queue(), cfg.exchange(), "#");
        } else {
            for (String key : bindingKeys) {
                channel.queueBind(cfg.queue(), cfg.exchange(), key);
            }
        }

        // Retry queues with TTL; on TTL expiry, message goes back to main exchange with original routing key
        for (Duration delay : cfg.retryBackoff()) {
            declareRetryQueue(channel, cfg, delay);
        }
    }

    private static void declareRetryQueue(Channel channel, RabbitMqTransportConfig cfg, Duration delay) throws IOException {
        String retryQueue = cfg.retryQueueName(delay);

        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", delay.toMillis());
        args.put("x-dead-letter-exchange", cfg.exchange());
        // Do NOT set x-dead-letter-routing-key → RabbitMQ uses original routing key (eventType)
        channel.queueDeclare(retryQueue, true, false, false, args);
    }
}
