# Request Validation

How to validate request data in nima applications.

## Using Bean Validation

Add the dependency:

```xml
<dependency>
  <groupId>jakarta.validation</groupId>
  <artifactId>jakarta.validation-api</artifactId>
  <version>3.0.0</version>
</dependency>

<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-validator</artifactId>
  <version>2.0</version>
</dependency>
```

Validate request objects:

```java
public class CreateUserRequest {
  @NotNull
  @NotBlank
  public String name;
  
  @NotNull
  @Email
  public String email;
  
  @Min(18)
  public int age;
}

@Controller
@Path("/users")
public class UserController {
  private final Validator validator;
  
  public UserController(Validator validator) {
    this.validator = validator;
  }
  
  @Post
  public User create(CreateUserRequest req) {
    Set<ConstraintViolation<CreateUserRequest>> violations = 
      validator.validate(req);
    
    if (!violations.isEmpty()) {
      List<String> errors = violations.stream()
        .map(v -> v.getPropertyPath() + ": " + v.getMessage())
        .toList();
      throw new ValidationException(errors);
    }
    
    return userService.create(req);
  }
}
```

## Common Validation Annotations

| Annotation | Purpose |
|-----------|---------|
| `@NotNull` | Value must not be null |
| `@NotBlank` | String must not be blank |
| `@NotEmpty` | Collection/array must not be empty |
| `@Size(min, max)` | String/collection size |
| `@Min(value)` | Number minimum |
| `@Max(value)` | Number maximum |
| `@Email` | Valid email format |
| `@Pattern(regex)` | Matches regex |
| `@Future` | Date must be in future |
| `@Past` | Date must be in past |

## Custom Validation

Create custom validators:

```java
@Constraint(validatedBy = UniqueEmailValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueEmail {
  String message() default "Email already exists";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
}

public class UniqueEmailValidator 
  implements ConstraintValidator<UniqueEmail, String> {
  
  private final UserService userService;
  
  public UniqueEmailValidator(UserService userService) {
    this.userService = userService;
  }
  
  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) return true;
    return !userService.emailExists(value);
  }
}

public class CreateUserRequest {
  @UniqueEmail
  public String email;
}
```

## Group Validation

Validate different groups for different operations:

```java
public interface CreateGroup {}
public interface UpdateGroup {}

public class UserRequest {
  @NotNull(groups = UpdateGroup.class)
  public Long id;
  
  @NotBlank(groups = { CreateGroup.class, UpdateGroup.class })
  public String name;
  
  @Email(groups = { CreateGroup.class, UpdateGroup.class })
  public String email;
}

@Controller
@Path("/users")
public class UserController {
  
  @Post
  public User create(UserRequest req) {
    validate(req, CreateGroup.class);
    return userService.create(req);
  }
  
  @Put(":id")
  public User update(long id, UserRequest req) {
    req.id = id;
    validate(req, UpdateGroup.class);
    return userService.update(req);
  }
  
  private void validate(UserRequest req, Class<?> group) {
    Set<ConstraintViolation<UserRequest>> violations = 
      validator.validate(req, group);
    
    if (!violations.isEmpty()) {
      List<String> errors = violations.stream()
        .map(v -> v.getMessage())
        .toList();
      throw new ValidationException(errors);
    }
  }
}
```

## Cascading Validation

Validate nested objects:

```java
public class User {
  @NotBlank
  public String name;
  
  @Valid
  @NotNull
  public Address address;
}

public class Address {
  @NotBlank
  public String street;
  
  @NotBlank
  public String city;
  
  @Pattern(regexp = "\\d{5}")
  public String zipCode;
}

@Controller
public class UserController {
  @Post("/users")
  public User create(CreateUserRequest req) {
    // Validates User AND nested Address
    validate(req);
    return userService.create(req);
  }
}
```

## Field-Level Validation

Validate individual fields:

```java
@NotBlank
public String validateEmail(String email) {
  if (!email.contains("@")) {
    throw new ValidationException("Invalid email format");
  }
  return email;
}
```

## Response Format

Return validation errors consistently:

```java
public class ValidationErrorResponse {
  public String message;
  public List<FieldError> errors;
  
  public static class FieldError {
    public String field;
    public String message;
    
    public FieldError(String field, String message) {
      this.field = field;
      this.message = message;
    }
  }
}

@ExceptionHandler
public ValidationErrorResponse handle(ValidationException e) {
  ValidationErrorResponse resp = new ValidationErrorResponse();
  resp.message = "Validation failed";
  resp.errors = e.errors.stream()
    .map(err -> new FieldError(err.field, err.message))
    .toList();
  return resp;
}
```

## Best Practices

| Practice | Reason |
|----------|--------|
| Validate on the server | Never trust client validation |
| Use bean validation annotations | Standard, declarative approach |
| Return field-level errors | Helps API consumers fix issues |
| Validate early | Fail fast on invalid requests |
| Test validation rules | Ensure error messages are clear |

## Next Steps

- Learn about [exception handling](exception-handling.md)
- Use [filters](filters.md) for validation middleware
- See [testing guide](testing.md) for testing validation
