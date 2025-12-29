public interface EventHandler<E extends Event> {
    void handle(E event) throws Exception;
}