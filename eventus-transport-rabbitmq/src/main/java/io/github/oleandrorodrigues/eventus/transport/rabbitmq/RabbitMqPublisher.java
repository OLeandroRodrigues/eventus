package io.github.oleandrorodrigues.eventus.transport.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import io.github.oleandrorodrigues.eventus.event.Event;

import java.io.IOException;
import java.util.Map;

public class RabbitMqPublisher {

    private final Channel channel;
    private final RabbitMqTransportConfig config;
    private final RabbitMqEventSerializer serializer;

    public RabbitMqPublisher(Channel channel,
                             RabbitMqTransportConfig config,
                             RabbitMqEventSerializer serializer) {
        this.channel = channel;
        this.config = config;
        this.serializer = serializer;
    }

    public void publish(Event event) throws IOException {
        byte[] body = serializer.serialize(event);

        channel.basicPublish(
                config.exchange(),
                event.eventType(),
                MessageProperties.PERSISTENT_BASIC,
                body
        );
    }
}
