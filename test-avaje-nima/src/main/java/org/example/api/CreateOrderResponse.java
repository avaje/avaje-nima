package org.example.api;

import io.avaje.jsonb.Json;

import java.time.Instant;

@Json
public record CreateOrderResponse (
  long id,
  Instant whenCreated
) {
}
