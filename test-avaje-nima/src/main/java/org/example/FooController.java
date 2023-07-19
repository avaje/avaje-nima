package org.example;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Path;
import io.avaje.jsonb.Json;

@Path("/foo")
@Controller
public class FooController {

  private final FooService fooService;

  FooController(FooService fooService) {
    this.fooService = fooService;
  }

  @Get
  Foo one() {
    long val = fooService.helloThere();
    return new Foo(82, "Foo here " + val);
  }

  @Json
  public record Foo(int id, String name) {

  }
}
