package org.example.web;

import io.avaje.http.api.*;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import org.example.api.ValidationErrorMessage;

@Controller
public class ErrorHandler {

  @Produces(statusCode = 422)
  @ExceptionHandler
  ValidationErrorMessage violation(ValidationException ex, ServerRequest req, ServerResponse res) {
    return new ValidationErrorMessage(422, ex.getMessage(), ex.getErrors());
  }
}
