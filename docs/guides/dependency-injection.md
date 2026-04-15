# Dependency Injection with Nima

How to configure dependency injection in nima applications.

## Using Avaje Inject

Add the dependency:

```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-inject</artifactId>
  <version>11.0</version>
</dependency>
```

## Basic Dependency Injection

Declare beans and inject them:

```java
@Singleton
public class UserService {
  private final UserRepository repository;
  
  public UserService(UserRepository repository) {
    this.repository = repository;
  }
  
  public User findById(long id) {
    return repository.findById(id);
  }
}

@Singleton
public class UserRepository {
  // Database access
}

@Controller
@Path("/users")
public class UserController {
  private final UserService userService;
  
  public UserController(UserService userService) {
    this.userService = userService;
  }
  
  @Get(":id")
  public User getUser(long id) {
    return userService.findById(id);
  }
}
```

## Scopes

Define the lifecycle of beans:

```java
// Singleton - one instance for application lifetime
@Singleton
public class DatabaseConnection { }

// Prototype - new instance for each request
@Bean
public class RequestContext { }

// Application scope
@ApplicationScoped
public class ConfigService { }
```

## Named Beans

Create multiple implementations:

```java
public interface Logger {
  void log(String message);
}

@Singleton
@Named("file")
public class FileLogger implements Logger {
  public void log(String message) {
    // Write to file
  }
}

@Singleton
@Named("console")
public class ConsoleLogger implements Logger {
  public void log(String message) {
    System.out.println(message);
  }
}

@Singleton
public class Service {
  private final Logger consoleLogger;
  private final Logger fileLogger;
  
  public Service(@Named("console") Logger console, 
                 @Named("file") Logger file) {
    this.consoleLogger = console;
    this.fileLogger = file;
  }
}
```

## Qualifiers

Use custom qualifiers for beans:

```java
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface Production { }

@Singleton
@Production
public class ProductionDatabase implements Database { }

@Singleton
public class Service {
  private final Database database;
  
  public Service(@Production Database database) {
    this.database = database;
  }
}
```

## Constructor Injection

Dependencies are injected through constructor:

```java
@Singleton
public class OrderService {
  private final UserService userService;
  private final PaymentService paymentService;
  
  public OrderService(
    UserService userService,
    PaymentService paymentService
  ) {
    this.userService = userService;
    this.paymentService = paymentService;
  }
}
```

## Field Injection

Inject into fields (less common):

```java
@Singleton
public class Service {
  @Inject
  private UserRepository repository;
  
  public void doSomething() {
    repository.save(...);
  }
}
```

## Factory Methods

Create beans with factory methods:

```java
@Factory
public class DatabaseFactory {
  
  @Bean
  public DataSource createDataSource() {
    return new HikariDataSource(...);
  }
  
  @Bean
  public Database createDatabase(DataSource ds) {
    return new Database(ds);
  }
}
```

## Lifecycle Methods

Hook into bean lifecycle:

```java
@Singleton
public class Service {
  
  @PostConstruct
  public void init() {
    System.out.println("Service initialized");
  }
  
  @PreDestroy
  public void shutdown() {
    System.out.println("Service shutting down");
  }
}
```

## Property Injection

Inject configuration properties:

```java
@Singleton
public class AppConfig {
  @Config("app.name")
  private String appName;
  
  @Config("app.version")
  private String appVersion;
}
```

## Optional Dependencies

Handle optional dependencies:

```java
@Singleton
public class Service {
  private final Optional<AnalyticsService> analytics;
  
  public Service(Optional<AnalyticsService> analytics) {
    this.analytics = analytics;
  }
  
  public void doSomething() {
    analytics.ifPresent(a -> a.track("event"));
  }
}
```

## Testing with DI

Create test beans:

```java
@Test
public class ServiceTest {
  
  @Test
  public void test() {
    UserRepository mockRepo = mock(UserRepository.class);
    UserService service = new UserService(mockRepo);
    
    // Test service
  }
}
```

## Best Practices

| Practice | Reason |
|----------|--------|
| Use constructor injection | Immutability, testability |
| Define scopes explicitly | Control resource lifecycle |
| Use qualifiers for multiple implementations | Avoid ambiguity |
| Keep factories simple | Easier to understand and test |
| Test with real beans when possible | Integration testing |

## Next Steps

- Learn about [testing](testing.md)
- Configure [filters](filters.md)
- See [controller basics](controller-basics.md)
