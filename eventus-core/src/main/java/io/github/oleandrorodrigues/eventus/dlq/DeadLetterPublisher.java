package io.github.oleandrorodrigues.eventus.dlq;

import io.github.oleandrorodrigues.eventus.event.Event;

public interface DeadLetterPublisher {
    void publish(Event event, Exception cause);
}