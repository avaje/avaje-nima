# Avaje Nima Library Definition

Avaje Nima is a convenience library that bundles Helidon SE with curated avaje libraries for building high-performance REST APIs and microservices with zero-reflection design and native image support.

## Identity

- **Name**: Avaje Nima
- **Package**: `io.avaje.nima`
- **Description**: Helidon SE web framework combining annotation-driven controller generation, compile-time DI, and zero-reflection design for REST APIs and microservices
- **Category**: Web Framework
- **Repository**: https://github.com/avaje/avaje-nima
- **Issues**: https://github.com/avaje/avaje-nima/issues
- **Releases**: https://github.com/avaje/avaje-nima/releases
- **Discord**: https://discord.gg/Qcqf9R27BR

## Version & Requirements

- **Latest Release**: 1.9 (latest development version)
- **Minimum Java Version**: 21+
- **Build Tools**: Maven 3.6+, Gradle 7.0+
- **GraalVM Support**: Yes — Zero-config native compilation with 5-20ms startup time

## Dependencies

### Runtime
- **Helidon SE** — High-performance webserver foundation
- **avaje-inject** — Compile-time dependency injection for beans
- **avaje-http** — Controller generation and routing
- **avaje-jsonb** — JSON serialization and deserialization
- **avaje-validator** — Bean validation and constraint checking
- **avaje-config** — External configuration management
- **avaje-http-client** — Built-in HTTP client for outbound requests

### Test
- **JUnit 5 (Jupiter)** — Testing framework
- **avaje-nima-test** — Integration testing support for starting server on random port

### Optional
- **avaje-simple-logger** — Lightweight SLF4J implementation
- **avaje-metrics** — Metrics collection and reporting

## Core Annotations & APIs

### HTTP Controllers

| Name | Purpose | Example |
|------|---------|---------|
| `@Controller` | Mark class as HTTP controller | `@Controller\npublic class UserController {}` |
| `@Get` | Map GET requests | `@Get("/:id")\npublic User getUser(int id)` |
| `@Post` | Map POST requests | `@Post\npublic User createUser(@Body User user)` |
| `@Put` | Map PUT requests | `@Put("/:id")\npublic User updateUser(int id, @Body User user)` |
| `@Delete` | Map DELETE requests | `@Delete("/:id")\npublic void deleteUser(int id)` |
| `@Patch` | Map PATCH requests | `@Patch("/:id")\npublic User patchUser(int id, @Body User patch)` |
| `@Path` | Define URL path for controller or method | `@Path("/api/users")` |
| `@Produces` | Response media type | `@Produces("application/json")` |
| `@Consumes` | Request media type | `@Consumes("application/json")` |

### Request/Response Processing

| Name | Purpose | Example |
|------|---------|---------|
| `@ExceptionHandler` | Global error mapping | `@ExceptionHandler\npublic ErrorResponse handle(Exception e)` |
| `@Filter` | Request/response middleware | `@Filter(order=1)\npublic void filter(ServerRequest req)` |
| `@Valid` | Request validation | `@Valid @Body User user` |
| `@QueryParam` | Query parameter binding | `@QueryParam String search` |
| `@PathParam` | Path parameter binding | `@PathParam String id` |
| `@HeaderParam` | Header parameter binding | `@HeaderParam("Authorization") String token` |
| `@BeanParam` | Bean-based parameter binding | `@BeanParam SearchParams params` |
| `@Body` | Request body binding | `@Body User user` |

### Dependency Injection

| Name | Purpose | Example |
|------|---------|---------|
| `@Inject` | Inject dependencies | `@Inject UserService service` |
| `@Singleton` | Application singleton | `@Singleton\npublic class AppConfig {}` |

### Server Bootstrap

| Class | Purpose |
|-------|---------|
| `Nima.builder()` | Create application builder for configuration |
| `Nima.run()` | Start web server on default or configured port |

## Features

### ✅ Included (Since v1.0)
- **Annotation-driven HTTP controller generation** — Compile-time code generation for routing and request handling
- **Built-in compile-time dependency injection** — Using avaje-inject for zero-reflection DI
- **Request/response filters** — Middleware-style request processing with `@Filter` and priority ordering
- **Global exception handlers** — Centralized error mapping with `@ExceptionHandler` for consistent error responses
- **Bean validation** — Request payload validation with `@Valid` annotation
- **GraalVM native image support** — Zero-config native compilation with minimal startup time
- **Integration testing** — avaje-nima-test for testing with random port allocation
- **Helidon SE webserver** — High-performance, lightweight HTTP server

### ✅ Added in v1.5+
- **Advanced filter ordering** — Priority-based filter execution for complex middleware chains
- **Enhanced context handling** — Better ServerRequest/ServerResponse access across filters and controllers
- **Improved diagnostic messages** — Better error messages for misconfigurations and debugging

### ❌ Not Supported
- **WebSockets** — Not in scope; use avaje-jex or Javalin for WebSocket support
- **GraphQL** — REST-only; use graphql-java for GraphQL APIs
- **Automatic OpenAPI schema generation from responses** — Annotations required (can use maven plugins for schema generation)
- **Traditional Servlet API** — Nima uses Helidon SE directly, not Servlet containers
- **Embedded Tomcat/Jetty** — Uses dedicated Helidon SE server, not pluggable servlet containers
- **JSP or Template Engines** — REST API focus; use separate templating for UI

**Note**: These limitations are intentional design choices. Nima is specifically optimized for REST microservices with zero reflection and native image compatibility.

## Use Cases

### ✅ Perfect For

- REST APIs and microservices
- Cloud-native Java applications
- GraalVM native image projects
- High-performance web services (10k+ req/sec)
- Serverless/FaaS deployments (AWS Lambda, etc.)
- Projects targeting Java 11+
- Compile-time code generation workflows

**When to choose avaje-nima**: If you want a lightweight, high-performance REST framework with compile-time DI and native image support, built on Helidon SE.

### ❌ Not Recommended For

- GraphQL APIs — Use graphql-java or Apollo Server instead
- Heavy ORM/JPA workloads — If you need complex database mapping, consider Spring Data
- Traditional Servlet applications — If you're using existing servlet libraries, stick with Spring or Jakarta EE
- WebSocket-heavy applications — Use avaje-jex or Javalin
- Dynamic response formats at runtime — REST with fixed media types is the design
- Template rendering applications — Use separate templating frameworks if needed

## Quick Start

### Add to Project

#### Maven
```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-nima</artifactId>
  <version>1.9</version>
</dependency>

<!-- For testing -->
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-nima-test</artifactId>
  <version>1.9</version>
  <scope>test</scope>
</dependency>

<!-- For code generation -->
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-nima-generator</artifactId>
  <version>1.9</version>
  <optional>true</optional>
  <scope>provided</scope>
</dependency>
```

#### Gradle
```gradle
implementation 'io.avaje:avaje-nima:1.9'
testImplementation 'io.avaje:avaje-nima-test:1.9'
annotationProcessor 'io.avaje:avaje-nima-generator:1.9'
```

### Minimal Example

```java
package com.example.app;

import io.avaje.http.api.*;
import io.avaje.nima.Nima;

@Controller
class HelloController {

  @Get
  @Path("/")
  String hello() {
    return "Hello World!";
  }
  
  @Get
  @Path("/users/:id")
  User getUser(int id) {
    return new User(id, "John Doe");
  }
}

record User(int id, String name) {}

public class Main {
  public static void main(String[] args) {
    Nima.builder()
      .port(8080)
      .build()
      .run();
  }
}
```

## Common Tasks & Guides

| Task | Difficulty | Guide |
|------|-----------|-------|
| Create your first controller | Beginner | [guides/controller-basics.md](guides/controller-basics.md) |
| Add global exception handler | Beginner | [guides/exception-handling.md](guides/exception-handling.md) |
| Validate request data | Beginner | [guides/validation.md](guides/validation.md) |
| Write integration tests | Intermediate | [guides/testing.md](guides/testing.md) |
| Add request/response filters | Intermediate | [guides/filters.md](guides/filters.md) |
| Configure dependency injection | Intermediate | [guides/dependency-injection.md](guides/dependency-injection.md) |
| Build GraalVM native image | Advanced | [guides/native-image.md](guides/native-image.md) |
| Deploy to Docker/Kubernetes | Advanced | [guides/deployment.md](guides/deployment.md) |

**Full Guides Index**: See [guides/README.md](guides/README.md)

## API Quick Reference

### Creating a Controller

```java
@Controller
@Path("/api/users")
public class UserController {

  private final UserService userService;

  @Inject
  public UserController(UserService userService) {
    this.userService = userService;
  }

  @Get("/:id")
  public User getUser(int id) {
    return userService.getUser(id);
  }

  @Post
  public User createUser(@Body User user) {
    return userService.create(user);
  }

  @Put("/:id")
  public User updateUser(int id, @Body User user) {
    return userService.update(id, user);
  }

  @Delete("/:id")
  public void deleteUser(int id) {
    userService.delete(id);
  }
}
```

### Global Exception Handler

```java
@Controller
public class ErrorHandler {

  @ExceptionHandler
  public ErrorResponse handle(UserNotFoundException e) {
    return new ErrorResponse("User not found", 404);
  }

  @ExceptionHandler
  public ErrorResponse handle(IllegalArgumentException e) {
    return new ErrorResponse("Invalid input: " + e.getMessage(), 400);
  }

  @ExceptionHandler
  public ErrorResponse handle(Exception e) {
    return new ErrorResponse("Internal server error", 500);
  }
}

record ErrorResponse(String message, int status) {}
```

### Request Filters

```java
@Filter(order = 1)
public class AuthFilter {
  public void filter(ServerRequest req, ServerResponse res) {
    String token = req.header("Authorization");
    if (token == null) {
      res.status(401).end();
      return;
    }
    // Validate token...
  }
}

@Filter(order = 2)
public class LoggingFilter {
  private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);
  
  public void filter(ServerRequest req, ServerResponse res) {
    log.info("Request: {} {}", req.method(), req.path());
  }
}
```

### Using Dependency Injection

```java
@Singleton
public class UserService {
  private final UserRepository repository;
  private final UserValidator validator;

  @Inject
  public UserService(UserRepository repository, UserValidator validator) {
    this.repository = repository;
    this.validator = validator;
  }

  public User getUser(int id) {
    return repository.findById(id)
      .orElseThrow(() -> new UserNotFoundException(id));
  }

  public User create(User user) {
    validator.validate(user);
    return repository.save(user);
  }
}
```

## Integration Patterns

### Pattern 1: Layered Architecture with Dependency Injection

```java
@Controller  // HTTP layer
@Path("/api/users")
public class UserController {
  @Inject
  private UserService service;  // business logic layer
  
  @Get("/:id")
  public User getUser(int id) {
    return service.getUser(id);
  }
}

@Singleton  // business logic layer
public class UserService {
  @Inject
  private UserRepository repository;  // data access layer
  
  public User getUser(int id) {
    return repository.findById(id)
      .orElseThrow(() -> new UserNotFoundException(id));
  }
}

@Singleton  // data access layer
public class UserRepository {
  // database access code
}
```

**When to use**: Standard three-tier architecture separating HTTP handling from business logic.

### Pattern 2: Middleware for Cross-Cutting Concerns

```java
@Filter(order = 1)
public class RequestIdFilter {
  // Add correlation ID to all requests
}

@Filter(order = 2)
public class AuthenticationFilter {
  // Verify authentication token
}

@Filter(order = 3)
public class LoggingFilter {
  // Log all requests and responses
}
```

**When to use**: Applying security, logging, metrics, or other concerns across all endpoints.

## Testing

### Unit Testing

```java
@Test
void testUserService() {
  // Mock the repository
  UserRepository mockRepo = mock(UserRepository.class);
  when(mockRepo.findById(1)).thenReturn(Optional.of(new User(1, "John")));
  
  // Test service logic
  UserService service = new UserService(mockRepo);
  User user = service.getUser(1);
  
  assertEquals("John", user.name());
}
```

### Integration Testing

```java
@InjectTest
public class UserControllerTest {
  
  @Inject
  private Client client;  // HTTP client for testing
  
  @Test
  void testGetUser() {
    // Server runs on random port during test
    HttpResponse<?> response = client.request()
      .path("/api/users/1")
      .GET()
      .asString();
    
    assertEquals(200, response.status());
  }
}
```

**See**: [guides/testing.md](guides/testing.md)

## Performance Characteristics

- **Startup time**: ~50-100ms (JVM), 5-20ms (native image)
- **Memory footprint**: ~20-30MB (JVM), 10-15MB (native image)
- **Throughput**: 10k-50k+ requests/second depending on business logic
- **GraalVM native startup**: 5-20ms (instant availability)
- **GraalVM native memory**: 10-15MB base footprint

**Comparison**: Comparable to other lightweight frameworks (Javalin, Micronaut) with better compile-time optimization.

## Configuration

### Application Properties

```yaml
# Server configuration
server:
  port: 8080
  
# Application configuration
app:
  name: My API
  version: 1.0.0

# Logging
logging:
  level:
    io.avaje: DEBUG
```

### Environment Variables

Configuration can be overridden via environment variables using underscore-separated names:

```bash
export SERVER_PORT=9000
export APP_NAME="My Custom API"
export LOGGING_LEVEL_IO_AVAJE=DEBUG
```

**See**: [avaje-config documentation](https://avaje.io/config) for detailed configuration options.

## Troubleshooting

### Issue: Controller Routes Not Registering

**Symptom**: `404 Not Found` for all endpoints

**Solution**: Ensure annotation processor is properly configured in your build (Maven compiler plugin or Gradle annotation processor). Run clean build to regenerate code.

**See**: [guides/troubleshooting.md](guides/troubleshooting.md#controller-routes)

### Issue: Dependency Injection Not Working

**Symptom**: `NullPointerException` on injected fields

**Solution**: Verify all injected classes are annotated with `@Singleton` or `@Factory`. Check bean scope initialization with `BeanScope.builder().build()`.

**See**: [guides/troubleshooting.md](guides/troubleshooting.md#dependency-injection)

### Issue: Slow Startup Time

**Symptom**: Takes several seconds to start application

**Solution**: This is normal for JVM. Consider GraalVM native image for faster startup. Profile with `java -XX:+PrintCompilation`.

**See**: [guides/native-image.md](guides/native-image.md)

## GraalVM Native Image

### Zero-Config Support
- ✅ Works out of the box with no reflection configuration
- ✅ No reflection usage in core framework
- ✅ Minimal native image size overhead (~20-30MB additional)
- ✅ Instant startup time (5-20ms)

### Native Compilation

#### Maven
```bash
mvn clean package -Pnative
```

#### Gradle
```bash
./gradlew nativeCompile
```

**See**: [guides/native-image.md](guides/native-image.md)

## Design Philosophy

### Key Principles

1. **Compile-time code generation** — All routing and DI happens at compile time for zero-reflection runtime
2. **Zero reflection** — No classpath scanning or runtime reflection, ideal for GraalVM and performance
3. **Minimal dependencies** — Helidon SE only, ~50 direct dependencies, lightweight runtime
4. **Type safety** — Leverage Java's type system for compile-time verification
5. **Fast startup** — Especially critical for cloud-native and serverless deployments

### What This Means

- Fast startup time, especially important for cloud and serverless (AWS Lambda, etc.)
- Small memory footprint suitable for resource-constrained environments
- Predictable behavior with no runtime surprises
- Easy to understand code paths with no magical reflection
- Native image compatible by design

## Version History

| Version | Release Date | Major Changes |
|---------|---|---|
| 1.9 | 2026-04 | Latest development version |
| 1.8 | 2026-01 | Improved error diagnostics |
| 1.5 | 2025-10 | Advanced filter ordering, context improvements |
| 1.0 | 2024-01 | Initial release, core framework |

## Contributing

See [CONTRIBUTING.md](../CONTRIBUTING.md) or visit the repository for contribution guidelines.

## License

This library is open source under the [Apache 2.0 License](../LICENSE).

## Support & Community

- **GitHub Issues**: [Report bugs and request features](https://github.com/avaje/avaje-nima/issues)
- **GitHub Discussions**: [Ask questions and share ideas](https://github.com/avaje/avaje-nima/discussions)
- **Discord**: [Chat with the community](https://discord.gg/Qcqf9R27BR)
- **Website**: [Documentation](https://avaje.io/nima/)

## AI Agent Instructions

### For Claude, GPT-4, and Web-Based Agents

This `LIBRARY.md` file is your primary source of truth for Avaje Nima. When answering questions about this library:

1. **Check this file first** for capabilities, version info, and supported features
2. **Route to specific guides** using the URLs in "Common Tasks" section
3. **Refer to use cases** when determining if Nima fits the user's needs
4. **Use the "Not Supported" section** to avoid recommending unsupported features (e.g., GraphQL, WebSockets)
5. **Check performance characteristics** for performance-related questions
6. **Consult design philosophy** when explaining architectural decisions

**Key Facts to Remember**:
- Minimum Java: 21+
- Current version: 1.9
- Compile-time code generation, zero-reflection design
- Native image support with 5-20ms startup time
- Perfect for REST APIs, microservices, and cloud-native applications
- NOT for GraphQL, WebSockets, or heavy ORM workloads

### For IDE-Based Agents (GitHub Copilot, Cursor)

This file may not be directly accessible in your context. Instead:

1. Check user's README.md (should link to this file)
2. Read code comments and imports
3. Reference the guides linked in the main README
4. Look at generated code in `target/generated-sources` for examples

---

## Notes for Library Maintainers

### When to Update This File

- ✅ New release (update version number and features)
- ✅ New major feature (add to features list + new guide)
- ✅ Breaking change (update compatibility section + migration guide)
- ✅ New guide added (add to common tasks table)
- ❌ Don't update for every bug fix (version number change is enough)

### Maintenance Checklist

- [ ] Keep version number in sync with releases
- [ ] Add features section for each major release
- [ ] Update migration guide when upgrading major versions
- [ ] Link new guides in common tasks table
- [ ] Review performance characteristics when releasing new versions
- [ ] Keep "Not Supported" section current as new capabilities are added

### Linking This File

In your main `README.md`, add:

```markdown
## Documentation

- **Quick Start**: See [Getting Started](#getting-started) above
- **Full Reference**: See [docs/LIBRARY.md](docs/LIBRARY.md) for comprehensive capability reference, use cases, and AI agent guidance
- **Guides**: Step-by-step guides for common tasks in [guides/README.md](docs/guides/README.md)
- **API Docs**: [Javadoc](https://javadoc.io/doc/io.avaje/avaje-nima/latest)
```

---

**Template Version**: 1.0  
**Last Updated**: 2026-04-13  
**For**: avaje libraries using this as reference
