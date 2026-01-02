package io.github.oleandrorodrigues.eventus.handler;

import io.github.oleandrorodrigues.eventus.event.Event;

public interface EventHandler<E extends Event> {
    void handle(E event) throws Exception;
}