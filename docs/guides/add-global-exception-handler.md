# Guide: Add a Global Exception Handler

## Purpose

This guide provides step-by-step instructions for adding a centralised exception
handler to an **avaje-nima** project. The handler catches exceptions thrown by any
controller, maps them to structured JSON error responses, and sets the correct HTTP
status codes — without repeating error-handling logic in every endpoint.

When asked to *"add a global exception handler"*, *"add centralised error handling"*,
or *"add an error response"* to an avaje-nima project, follow these steps exactly.

---

## Overview

The pattern uses two classes in a `web/exception` package:

| Class | Purpose |
|---|---|
| `ErrorResponse` | JSON record returned in the response body for all errors |
| `GlobalExceptionController` | `@Controller` with `@ExceptionHandler` methods; one per exception type |

avaje's annotation processor generates the routing glue at compile time — no runtime
configuration needed.

> `avaje-nima` already transitively includes `avaje-jsonb` (`@Json`) and
> `avaje-http-api` (`@ExceptionHandler`). Only `avaje-record-builder` needs to be
> added explicitly.

---

## Step 1 — Add the `avaje-record-builder` dependency to `pom.xml`

`ErrorResponse` uses `@RecordBuilder` to generate a builder. Add the dependency to
`pom.xml` as a `provided`-scope annotation processor:

```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-record-builder</artifactId>
  <version>1.4</version>
  <scope>provided</scope>
</dependency>
```

---

## Step 2 — Create `ErrorResponse.java`

Create the file at `src/main/java/<base-package>/web/exception/ErrorResponse.java`.

Replace `<base-package>` with the project's root package (find it by looking at
existing controller imports or `module-info.java`).

```java
package <base-package>.web.exception;

import io.avaje.jsonb.Json;
import io.avaje.recordbuilder.RecordBuilder;

@Json
@RecordBuilder
public record ErrorResponse(
    int httpCode,
    String path,
    String message,
    String traceId
) {
    public static ErrorResponseBuilder builder() {
        return ErrorResponseBuilder.builder();
    }
}
```

**Fields:**

| Field | Description |
|---|---|
| `httpCode` | The HTTP status code (e.g. `400`, `404`, `500`) |
| `path` | The request path where the error occurred |
| `message` | A human-readable description of the error |
| `traceId` | Distributed trace ID (set to `null` until tracing is integrated) |

> `@RecordBuilder` instructs the `avaje-record-builder` processor to generate
> `ErrorResponseBuilder` at compile time. The static `builder()` method delegates to
> the generated builder.

---

## Step 3 — Create `GlobalExceptionController.java`

Create the file at
`src/main/java/<base-package>/web/exception/GlobalExceptionController.java`:

```java
package <base-package>.web.exception;

import io.avaje.http.api.Controller;
import io.avaje.http.api.ExceptionHandler;
import io.avaje.http.api.Produces;
import io.helidon.http.BadRequestException;
import io.helidon.http.InternalServerException;
import io.helidon.http.NotFoundException;
import io.helidon.webserver.http.ServerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
final class GlobalExceptionController {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionController.class);

    private static final int HTTP_500 = 500;
    private static final int HTTP_400 = 400;
    private static final int HTTP_404 = 404;

    private static final String HTTP_500_MESSAGE = "An error occurred processing the request.";
    private static final String HTTP_404_MESSAGE = "Not found for ";

    @Produces(statusCode = HTTP_500)
    @ExceptionHandler
    ErrorResponse defaultErrorResponse(Exception ex, ServerRequest req) {
        logException(ex, path(req));
        return ErrorResponse.builder()
            .httpCode(HTTP_500)
            .path(path(req))
            .message(HTTP_500_MESSAGE)
            .build();
    }

    @Produces(statusCode = HTTP_400)
    @ExceptionHandler
    ErrorResponse badRequest(BadRequestException ex, ServerRequest req) {
        logException(ex, path(req));
        return ErrorResponse.builder()
            .httpCode(HTTP_400)
            .path(path(req))
            .message(ex.getMessage())
            .build();
    }

    @Produces(statusCode = HTTP_500)
    @ExceptionHandler
    ErrorResponse internalServerError(InternalServerException ex, ServerRequest req) {
        logException(ex, path(req));
        return ErrorResponse.builder()
            .httpCode(HTTP_500)
            .path(path(req))
            .message(HTTP_500_MESSAGE)
            .build();
    }

    @Produces(statusCode = HTTP_400)
    @ExceptionHandler(UnsupportedOperationException.class)
    ErrorResponse unsupportedOperation(UnsupportedOperationException ex, ServerRequest req) {
        return ErrorResponse.builder()
            .httpCode(HTTP_400)
            .path(path(req))
            .message(ex.getMessage())
            .traceId(null)
            .build();
    }

    @Produces(statusCode = HTTP_404)
    @ExceptionHandler(NotFoundException.class)
    ErrorResponse notFound(ServerRequest req) {
        String path = path(req);
        log.debug("404 not found path:{}", path);
        return ErrorResponse.builder()
            .httpCode(HTTP_404)
            .path(path)
            .message(HTTP_404_MESSAGE + path)
            .traceId(null)
            .build();
    }

    private static void logException(Exception ex, String path) {
        log.error("An error occurred processing request on path '{}'", path, ex);
    }

    private static String path(ServerRequest req) {
        return req != null && req.path() != null
            ? req.path().path()
            : null;
    }
}
```

---

## Step 4 — Key rules to follow

1. **`GlobalExceptionController` must be package-private** (`final class`, no `public`).
   avaje-inject discovers it from generated wiring regardless of visibility.
2. **`ErrorResponse` must be `public`** — it is part of the JSON API surface.
3. Both files go in the **same `web/exception` package** (or equivalent sub-package).
4. The `@ExceptionHandler` exception type is inferred from the first parameter; use
   `@ExceptionHandler(SomeException.class)` when the parameter type differs or is
   omitted (e.g. the 404 handler which has no exception parameter).
5. Always pair `@ExceptionHandler` with `@Produces(statusCode = N)` to set the correct
   HTTP status.

### Handler method signature rules

avaje-http maps `@ExceptionHandler` methods by inspecting the first parameter type:

```java
// Implicit — exception type inferred from first parameter
@ExceptionHandler
ErrorResponse handle(BadRequestException ex, ServerRequest req) { … }

// Explicit — handles exactly UnsupportedOperationException
@ExceptionHandler(UnsupportedOperationException.class)
ErrorResponse handle(UnsupportedOperationException ex, ServerRequest req) { … }

// No exception parameter — handler receives only the request (e.g. for 404)
@ExceptionHandler(NotFoundException.class)
ErrorResponse handle(ServerRequest req) { … }
```

### Handler priority

More-specific exception types take priority over broader ones. The `Exception`
catch-all is the fallback:

```
NotFoundException             → 404
BadRequestException           → 400
InternalServerException       → 500
UnsupportedOperationException → 400
Exception (catch-all)         → 500
```

---

## Step 5 — Verify

```bash
mvn compile
```

The build must succeed with no errors from the annotation processors. Then run and test:

```bash
curl -i http://localhost:8080/no-such-path
# Expected: HTTP 404, Content-Type: application/json
```

Expected response body:

```json
{"httpCode":404,"path":"/no-such-path","message":"Not found for /no-such-path","traceId":null}
```

---

## Notes

- The `traceId` field is always `null` in this baseline. Populate it with a distributed
  trace ID (e.g. from a `traceparent` header) when tracing is integrated:
  ```java
  String traceId = req.headers().value(HeaderNames.create("traceparent")).orElse(null);
  ```
- To add handlers for additional exception types, add new methods following the same
  pattern: `@Produces(statusCode = N)` + `@ExceptionHandler` + exception type as first
  parameter.
- The `Exception` catch-all is the fallback. More-specific types always take priority.

---

## Version compatibility

| Component | Tested version |
|---|---|
| `avaje-record-builder` | 1.4 |
| `avaje-nima` (includes `avaje-http-api`, `avaje-jsonb`) | 1.8 |
| Helidon SE | 4.4.0 |
| Java | 25 |

---

## References

- avaje-http `@ExceptionHandler` docs: https://avaje.io/http/
- avaje-record-builder: https://github.com/avaje/avaje-record-builder
