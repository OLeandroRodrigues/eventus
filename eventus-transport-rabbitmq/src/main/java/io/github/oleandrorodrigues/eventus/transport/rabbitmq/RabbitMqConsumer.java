package io.github.oleandrorodrigues.eventus.transport.rabbitmq;

import com.rabbitmq.client.*;
import io.github.oleandrorodrigues.eventus.bus.EventBus;
import io.github.oleandrorodrigues.eventus.event.Event;

import java.io.IOException;

public class RabbitMqConsumer {

    private final Channel channel;
    private final RabbitMqTransportConfig config;
    private final RabbitMqEventDeserializer deserializer;
    private final EventBus eventBus;

    public RabbitMqConsumer(Channel channel,
                            RabbitMqTransportConfig config,
                            RabbitMqEventDeserializer deserializer,
                            EventBus eventBus) {
        this.channel = channel;
        this.config = config;
        this.deserializer = deserializer;
        this.eventBus = eventBus;
    }

    public void start() throws IOException {
        channel.basicConsume(
                config.queue(),
                false,
                new DefaultConsumer(channel) {
                    @Override
                    public void handleDelivery(String tag,
                                               Envelope envelope,
                                               AMQP.BasicProperties props,
                                               byte[] body) throws IOException {
                        try {
                            Event event = deserializer.deserialize(body);
                            eventBus.publish(event);
                            channel.basicAck(envelope.getDeliveryTag(), false);
                        } catch (Exception ex) {
                            channel.basicReject(envelope.getDeliveryTag(), false);
                        }
                    }
                }
        );
    }
}