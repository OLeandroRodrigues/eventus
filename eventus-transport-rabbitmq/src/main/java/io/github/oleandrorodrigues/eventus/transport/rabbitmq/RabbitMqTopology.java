package io.github.oleandrorodrigues.eventus.transport.rabbitmq;

import com.rabbitmq.client.Channel;

import java.io.IOException;
import java.util.Map;

public final class RabbitMqTopology {

    private RabbitMqTopology() {}

    public static void declare(Channel channel, RabbitMqTransportConfig config) throws IOException {
        channel.exchangeDeclare(config.exchange(), "topic", true);

        channel.exchangeDeclare(config.deadLetterExchange(), "topic", true);

        channel.queueDeclare(
                config.queue(),
                true,
                false,
                false,
                Map.of(
                        "x-dead-letter-exchange", config.deadLetterExchange()
                )
        );
    }
}
