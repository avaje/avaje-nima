package org.example.api;

import io.avaje.http.api.ValidationException.Violation;
import io.avaje.jsonb.Json;

import java.util.List;

@Json
public record ValidationErrorMessage(int status, String message, List<Violation> errors) {

  public Violation forPath(String name) {
    return errors.stream()
      .filter(v -> name.equals(v.getPath()))
      .findFirst().orElseThrow();
  }
}
