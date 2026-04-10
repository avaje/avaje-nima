package ${package}.model;

import io.avaje.jsonb.Json;

@Json
public record GreetingResponse(String message, long timestamp) {
}
