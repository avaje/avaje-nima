# Request/Response Filters

How to use filters and middleware in nima applications.

## Creating a Filter

Implement the filter interface:

```java
import io.avaje.nima.Filter;
import io.avaje.nima.FilterChain;
import io.avaje.nima.Context;

public class LoggingFilter implements Filter {
  private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);
  
  @Override
  public void doFilter(Request request, Response response, FilterChain chain) 
    throws Exception {
    
    long startTime = System.currentTimeMillis();
    
    try {
      log.info("Request: {} {}", request.method(), request.path());
      chain.doFilter(request, response);
    } finally {
      long duration = System.currentTimeMillis() - startTime;
      log.info("Response: {} - {} ms", response.status(), duration);
    }
  }
}
```

## Registering Filters

Register filters in your application:

```java
public class Application {
  public static void main(String[] args) {
    Server server = Server.builder()
      .addFilter(new LoggingFilter())
      .addFilter(new AuthenticationFilter())
      .addFilter(new CorsFilter())
      .build()
      .start();
  }
}
```

## Common Filters

### Authentication Filter

```java
public class AuthenticationFilter implements Filter {
  @Override
  public void doFilter(Request request, Response response, FilterChain chain) 
    throws Exception {
    
    String token = request.header("Authorization");
    
    if (token == null || !isValidToken(token)) {
      response.status(401);
      response.text("Unauthorized");
      return;
    }
    
    // Continue to next filter
    chain.doFilter(request, response);
  }
  
  private boolean isValidToken(String token) {
    // Validate JWT or session token
    return true;
  }
}
```

### CORS Filter

```java
public class CorsFilter implements Filter {
  @Override
  public void doFilter(Request request, Response response, FilterChain chain) 
    throws Exception {
    
    response.header("Access-Control-Allow-Origin", "*");
    response.header("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
    response.header("Access-Control-Allow-Headers", "Content-Type,Authorization");
    
    if ("OPTIONS".equals(request.method())) {
      response.status(200);
      return;
    }
    
    chain.doFilter(request, response);
  }
}
```

### Request/Response Logging Filter

```java
public class DetailedLoggingFilter implements Filter {
  private static final Logger log = 
    LoggerFactory.getLogger(DetailedLoggingFilter.class);
  
  @Override
  public void doFilter(Request request, Response response, FilterChain chain) 
    throws Exception {
    
    log.debug("Request Headers: {}", request.headerMap());
    log.debug("Request Body: {}", request.body());
    
    chain.doFilter(request, response);
    
    log.debug("Response Status: {}", response.status());
    log.debug("Response Headers: {}", response.headerMap());
  }
}
```

### Exception Handling Filter

```java
public class ExceptionHandlingFilter implements Filter {
  private static final Logger log = 
    LoggerFactory.getLogger(ExceptionHandlingFilter.class);
  
  @Override
  public void doFilter(Request request, Response response, FilterChain chain) 
    throws Exception {
    
    try {
      chain.doFilter(request, response);
    } catch (ValidationException e) {
      log.warn("Validation error: {}", e.getMessage());
      response.status(400);
      response.json(new { message = e.getMessage() });
    } catch (NotFoundException e) {
      log.warn("Not found: {}", e.getMessage());
      response.status(404);
      response.json(new { message = e.getMessage() });
    } catch (Exception e) {
      log.error("Unexpected error", e);
      response.status(500);
      response.json(new { message = "Internal Server Error" });
    }
  }
}
```

## Filter Ordering

Filters execute in registration order:

```java
Server.builder()
  .addFilter(new ExceptionHandlingFilter())     // First
  .addFilter(new AuthenticationFilter())        // Second
  .addFilter(new LoggingFilter())               // Third
  .build()
  .start();
```

Request flows through in order: ExceptionHandling → Authentication → Logging → Controller

## Path-Specific Filters

Apply filters to specific paths:

```java
public class AdminAuthFilter implements Filter {
  @Override
  public void doFilter(Request request, Response response, FilterChain chain) 
    throws Exception {
    
    if (request.path().startsWith("/admin")) {
      if (!isAdmin(request)) {
        response.status(403);
        response.text("Forbidden");
        return;
      }
    }
    
    chain.doFilter(request, response);
  }
}
```

## Intercepting Request Body

Read and modify request body:

```java
public class RequestBodyFilter implements Filter {
  @Override
  public void doFilter(Request request, Response response, FilterChain chain) 
    throws Exception {
    
    String body = request.body();
    
    if (body != null && body.contains("sensitive")) {
      // Sanitize or reject
      response.status(400);
      response.text("Invalid input");
      return;
    }
    
    chain.doFilter(request, response);
  }
}
```

## Performance Considerations

- Keep filters lightweight
- Avoid blocking I/O in filters
- Cache expensive computations
- Order filters by frequency of use

## Best Practices

| Practice | Reason |
|----------|--------|
| Order filters properly | Execution sequence matters |
| Log at appropriate levels | Debug vs production clarity |
| Handle exceptions gracefully | Prevent filter chain breakage |
| Use specific conditions | Apply filters only when needed |
| Test filters independently | Ensure correct behavior |

## Next Steps

- Learn about [dependency injection](dependency-injection.md)
- See [exception handling](exception-handling.md)
- Check [testing guide](testing.md) for filter testing
