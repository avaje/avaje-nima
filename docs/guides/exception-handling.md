# Exception Handling

How to handle errors gracefully in nima applications.

## Global Exception Handler

Create a global exception handler:

```java
import io.avaje.nima.ExceptionHandler;
import io.avaje.nima.Context;

@ExceptionHandler
public class GlobalExceptionHandler {
  
  public ErrorResponse handle(Exception e, Context ctx) {
    int status = 500;
    String message = "Internal Server Error";
    
    if (e instanceof ValidationException) {
      status = 400;
      message = e.getMessage();
    } else if (e instanceof ResourceNotFoundException) {
      status = 404;
      message = e.getMessage();
    } else if (e instanceof UnauthorizedException) {
      status = 401;
      message = e.getMessage();
    }
    
    ctx.status(status);
    return new ErrorResponse(status, message);
  }
}

public class ErrorResponse {
  public int status;
  public String message;
  
  public ErrorResponse(int status, String message) {
    this.status = status;
    this.message = message;
  }
}
```

## Controller-Level Exception Handling

Handle exceptions per controller:

```java
@Controller
@Path("/users")
public class UserController {
  
  @ExceptionHandler
  public ErrorResponse handleException(Exception e, Context ctx) {
    ctx.status(500);
    return new ErrorResponse(500, e.getMessage());
  }
  
  @Get(":id")
  public User getUser(long id) {
    return userService.findById(id)
      .orElseThrow(() -> new ResourceNotFoundException("User not found"));
  }
}
```

## Custom Exceptions

Create custom exceptions:

```java
public class ResourceNotFoundException extends RuntimeException {
  public ResourceNotFoundException(String message) {
    super(message);
  }
}

public class ValidationException extends RuntimeException {
  public final List<String> errors;
  
  public ValidationException(List<String> errors) {
    super("Validation failed");
    this.errors = errors;
  }
}

public class UnauthorizedException extends RuntimeException {
  public UnauthorizedException(String message) {
    super(message);
  }
}
```

## Handling Specific Exceptions

Return different responses based on exception type:

```java
@ExceptionHandler
public Response<?> handle(Exception e, Context ctx) {
  
  if (e instanceof ValidationException) {
    ctx.status(400);
    return Response.of(new {
      errors = ((ValidationException) e).errors,
      message = "Validation failed"
    });
  }
  
  if (e instanceof ResourceNotFoundException) {
    ctx.status(404);
    return Response.of(new {
      message = e.getMessage()
    });
  }
  
  if (e instanceof UnauthorizedException) {
    ctx.status(401);
    return Response.of(new {
      message = "Unauthorized"
    });
  }
  
  // Default 500
  ctx.status(500);
  return Response.of(new {
    message = "Internal Server Error"
  });
}
```

## Validation Error Responses

Return validation errors with details:

```java
@Controller
@Path("/items")
public class ItemController {
  
  @Post
  public Item create(CreateItemRequest req) {
    List<String> errors = new ArrayList<>();
    
    if (req.name == null || req.name.isEmpty()) {
      errors.add("name is required");
    }
    if (req.price <= 0) {
      errors.add("price must be positive");
    }
    
    if (!errors.isEmpty()) {
      throw new ValidationException(errors);
    }
    
    return itemService.create(req);
  }
}

// Response:
// {
//   "message": "Validation failed",
//   "errors": ["name is required", "price must be positive"]
// }
```

## Logging Exceptions

Log exceptions while handling:

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExceptionHandler
public Response<?> handle(Exception e, Context ctx) {
  Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
  
  if (e instanceof ValidationException) {
    log.warn("Validation error: {}", e.getMessage());
    ctx.status(400);
  } else {
    log.error("Unexpected error", e);
    ctx.status(500);
  }
  
  return Response.of(new { message = e.getMessage() });
}
```

## Async Error Handling

Handle errors in async operations:

```java
@Controller
@Path("/async")
public class AsyncController {
  
  @Get
  public CompletableFuture<Data> fetchAsync() {
    return dataService.fetchAsync()
      .exceptionally(e -> {
        logger.error("Async fetch failed", e);
        throw new RuntimeException("Failed to fetch data");
      });
  }
}
```

## Best Practices

| Practice | Reason |
|----------|--------|
| Use specific exception types | Better error categorization |
| Return meaningful error messages | Helps API consumers debug |
| Log all exceptions | Essential for debugging |
| Use consistent error format | Better API experience |
| Don't expose stack traces to clients | Security and user experience |

## Next Steps

- Add [validation](validation.md) to prevent invalid data
- Use [filters](filters.md) for cross-cutting concerns
- See [troubleshooting](troubleshooting.md) for common errors
