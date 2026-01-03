package io.github.oleandrorodrigues.eventus.transport.rabbitmq;

public record RabbitMqTransportConfig(
        String host,
        int port,
        String username,
        String password,
        String exchange,
        String queue,
        String deadLetterExchange
) {}
