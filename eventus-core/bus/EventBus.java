import java.beans.EventHandler;

public interface EventBus {
    void publish(Event event);
    void subscribe(String eventType, EventHandler<? extends Event> handler);
}