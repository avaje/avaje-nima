package org.example.web;

import io.avaje.applog.AppLog;
import io.avaje.http.api.*;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import org.example.api.ValidationErrorMessage;

@Controller
public class ErrorHandler {

  private static final System.Logger log = AppLog.getLogger(ErrorHandler.class);

  @Produces(statusCode = 422)
  @ExceptionHandler
  ValidationErrorMessage violation(ValidationException ex, ServerRequest req, ServerResponse res) {
    return new ValidationErrorMessage(422, ex.getMessage(), ex.getErrors());
  }

  @Produces(statusCode = 500)
  @ExceptionHandler
  String general(Throwable ex, ServerRequest req, ServerResponse res) {
    log.log(System.Logger.Level.ERROR, "Unhandled error - " + ex, ex);
    return ex.getMessage();
  }
}
