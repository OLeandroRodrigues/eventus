package io.github.oleandrorodrigues.eventus.bus;

import io.github.oleandrorodrigues.eventus.handler.EventHandler;
import io.github.oleandrorodrigues.eventus.event.Event;

public interface EventBus {
    void publish(Event event);
    void subscribe(String eventType, EventHandler<? extends Event> handler);
}