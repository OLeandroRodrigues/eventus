import java.util.Map;

public record EventMetadata(
    String correlationId,
    String causationId,
    String source,
    Map<String, String> attributes
) {}