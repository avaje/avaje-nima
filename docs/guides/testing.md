# Testing Nima Applications

How to write tests for nima web applications.

## Setting Up Tests

Add test dependencies:

```xml
<dependency>
  <groupId>org.junit.jupiter</groupId>
  <artifactId>junit-jupiter</artifactId>
  <version>5.9.0</version>
  <scope>test</scope>
</dependency>

<dependency>
  <groupId>io.avaje.test</groupId>
  <artifactId>avaje-http-test</artifactId>
  <version>2.1</version>
  <scope>test</scope>
</dependency>
```

## Basic HTTP Tests

Test controllers using the test client:

```java
import io.avaje.http.test.HttpTest;
import io.avaje.http.test.Client;

@HttpTest
public class UserControllerTest {
  
  Client httpClient;
  
  @Test
  public void testGetUser() {
    httpClient.request()
      .get("/users/1")
      .expect()
      .status(200)
      .contains("\"id\":1");
  }
  
  @Test
  public void testCreateUser() {
    httpClient.request()
      .post("/users")
      .body(new { name = "John", email = "john@example.com" })
      .expect()
      .status(201);
  }
  
  @Test
  public void testUserNotFound() {
    httpClient.request()
      .get("/users/999")
      .expect()
      .status(404);
  }
}
```

## Asserting Responses

Verify response content:

```java
@Test
public void testJsonResponse() {
  httpClient.request()
    .get("/api/users/1")
    .expect()
    .status(200)
    .contains("\"name\":\"John\"")
    .contains("\"email\":\"john@example.com\"");
}

@Test
public void testResponseHeaders() {
  httpClient.request()
    .get("/api/data")
    .expect()
    .status(200)
    .header("Content-Type", "application/json")
    .header("X-Custom-Header", "value");
}
```

## Request Building

Build complex requests:

```java
@Test
public void testWithHeaders() {
  httpClient.request()
    .get("/protected")
    .header("Authorization", "Bearer token123")
    .header("Accept", "application/json")
    .expect()
    .status(200);
}

@Test
public void testWithQueryParams() {
  httpClient.request()
    .get("/search?query=java&page=1&per_page=10")
    .expect()
    .status(200);
}

@Test
public void testPostWithBody() {
  CreateUserRequest req = new CreateUserRequest("John", "john@example.com");
  
  httpClient.request()
    .post("/users")
    .body(req)
    .expect()
    .status(201);
}
```

## Mocking Dependencies

Mock service dependencies:

```java
@HttpTest
public class UserControllerTest {
  
  @Mock
  private UserService userService;
  
  Client httpClient;
  
  @Test
  public void testGetUser() {
    User user = new User(1, "John", "john@example.com");
    when(userService.findById(1)).thenReturn(user);
    
    httpClient.request()
      .get("/users/1")
      .expect()
      .status(200)
      .contains("\"name\":\"John\"");
  }
  
  @Test
  public void testUserNotFound() {
    when(userService.findById(999))
      .thenThrow(new NotFoundException("User not found"));
    
    httpClient.request()
      .get("/users/999")
      .expect()
      .status(404);
  }
}
```

## Testing Exception Handling

Test error responses:

```java
@Test
public void testValidationError() {
  httpClient.request()
    .post("/users")
    .body(new { name = "" })  // Invalid - blank name
    .expect()
    .status(400)
    .contains("name is required");
}

@Test
public void testInternalError() {
  when(userService.findById(1))
    .thenThrow(new RuntimeException("Database error"));
  
  httpClient.request()
    .get("/users/1")
    .expect()
    .status(500)
    .contains("Internal Server Error");
}
```

## Integration Testing

Test with real dependencies:

```java
@ExtendWith(PostgreSQLExtension.class)
public class UserControllerIntegrationTest {
  
  @Container
  static PostgreSQLContainer db = new PostgreSQLContainer<>()
    .withDatabaseName("test_db")
    .withUsername("test")
    .withPassword("test");
  
  Client httpClient;
  
  @Test
  public void testCreateAndRetrieveUser() {
    // Create user
    httpClient.request()
      .post("/users")
      .body(new { name = "John", email = "john@example.com" })
      .expect()
      .status(201);
    
    // Retrieve user
    httpClient.request()
      .get("/users/1")
      .expect()
      .status(200)
      .contains("\"name\":\"John\"");
  }
}
```

## Test Fixtures

Reuse common setup:

```java
@HttpTest
public class UserControllerTest {
  
  Client httpClient;
  
  @BeforeEach
  public void setUp() {
    // Create test users
    userService.create(new User("user1", "user1@example.com"));
    userService.create(new User("user2", "user2@example.com"));
  }
  
  @Test
  public void testListUsers() {
    httpClient.request()
      .get("/users")
      .expect()
      .status(200)
      .contains("user1")
      .contains("user2");
  }
}
```

## Best Practices

| Practice | Reason |
|----------|--------|
| Test both success and failure paths | Complete coverage |
| Use realistic test data | Better catch real bugs |
| Mock external services | Tests are fast and isolated |
| Test error responses | Users need clear error messages |
| Keep tests focused | Single assertion per test |

## Next Steps

- See [testing configuration](../../../docs/guides/testing.md) for test setup
- Learn about [filters](filters.md)
- See [troubleshooting](troubleshooting.md) for test issues
