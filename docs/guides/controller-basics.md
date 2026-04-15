# Creating Your First REST Controller

How to create a basic REST controller with avaje-nima.

## Installation

Add the dependency:

```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-nima</artifactId>
  <version>2.1</version>
</dependency>
```

## Simple Controller

Create a controller class:

```java
import io.avaje.nima.Controller;
import io.avaje.nima.Get;
import io.avaje.nima.Post;
import io.avaje.nima.Path;

@Controller
@Path("/users")
public class UserController {
  
  @Get
  public String list() {
    return "List of users";
  }
  
  @Get("/:id")
  public String get(long id) {
    return "User " + id;
  }
  
  @Post
  public String create(CreateUserRequest req) {
    return "Created user: " + req.name;
  }
}
```

## Request/Response

Handle JSON automatically:

```java
@Controller
@Path("/api/users")
public class UserApi {
  
  @Get(":id")
  public User getUser(long id) {
    // Automatically serialized to JSON
    return userService.findById(id);
  }
  
  @Post
  public User create(CreateUserRequest req) {
    // Automatically deserialized from JSON
    return userService.create(req.name, req.email);
  }
}

public class User {
  public long id;
  public String name;
  public String email;
  
  // getters/setters or records
}

public class CreateUserRequest {
  public String name;
  public String email;
}
```

Or using Records:

```java
public record User(long id, String name, String email) {}
public record CreateUserRequest(String name, String email) {}
```

## HTTP Methods

All HTTP methods are supported:

```java
@Controller
@Path("/posts/:id")
public class PostController {
  
  @Get
  public Post get(long id) { }
  
  @Post
  public Post create(CreatePostRequest req) { }
  
  @Put
  public Post update(long id, UpdatePostRequest req) { }
  
  @Delete
  public void delete(long id) { }
  
  @Patch
  public Post patch(long id, JsonPatch patch) { }
}
```

## Response Status

Set HTTP status codes:

```java
@Controller
public class ItemController {
  
  @Post
  public Response<Item> create(CreateItemRequest req) {
    Item item = itemService.create(req);
    return Response.of(item).status(201);  // Created
  }
  
  @Delete(":id")
  public void delete(long id) {
    itemService.delete(id);
    return Response.noContent();  // 204 No Content
  }
  
  @Get(":id")
  public Item get(long id) {
    return itemService.findById(id)
      .orElse(Response.notFound());  // 404 Not Found
  }
}
```

## Path Parameters

Extract values from the URL path:

```java
@Controller
@Path("/users/:userId/posts/:postId")
public class PostController {
  
  @Get
  public Post get(long userId, long postId) {
    // Both userId and postId are extracted from path
    return postService.findByUserAndId(userId, postId);
  }
}

// Call: GET /users/123/posts/456
// userId = 123, postId = 456
```

## Query Parameters

Handle query string parameters:

```java
@Controller
@Path("/search")
public class SearchController {
  
  @Get
  public List<Item> search(
    @Query String query,
    @Query int page,
    @Query("per_page") int pageSize
  ) {
    return itemService.search(query, page, pageSize);
  }
}

// Call: GET /search?query=java&page=1&per_page=10
```

## Headers

Access request headers:

```java
@Controller
public class AuthController {
  
  @Get("/protected")
  public String getProtected(
    @Header("Authorization") String auth,
    @Header("X-Custom-Header") String custom
  ) {
    return "Auth: " + auth;
  }
}
```

## Request Body

Handle request body:

```java
@Controller
@Path("/api/items")
public class ItemApi {
  
  @Post
  public Item create(@Body CreateItemRequest req) {
    return itemService.create(req);
  }
  
  @Put(":id")
  public Item update(long id, @Body UpdateItemRequest req) {
    return itemService.update(id, req);
  }
}
```

## Next Steps

- Learn about [exception handling](exception-handling.md)
- Add [request validation](validation.md)
- Use [filters & middleware](filters.md)
