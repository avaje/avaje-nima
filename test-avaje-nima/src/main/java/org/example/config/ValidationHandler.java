package org.example.config;

import io.avaje.http.api.ValidationException;
import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.stream.JsonOutput;
import io.helidon.nima.webserver.http.HttpRouting;
import org.example.api.ValidationErrorMessage;

@Factory
class ValidationHandler  {

  private final JsonType<ValidationErrorMessage> jsonType;

  public ValidationHandler(Jsonb jsonb) {
    this.jsonType = jsonb.type(ValidationErrorMessage.class);
  }

  @Bean
  void accept(HttpRouting.Builder builder) {
    builder.error(ValidationException.class, (req, res, exception) -> {
      final var errorMessage = new ValidationErrorMessage(422, exception.getMessage(), exception.getErrors());

      res.status(422);
      jsonType.toJson(errorMessage, JsonOutput.of(res.outputStream()));
    });
  }


}
