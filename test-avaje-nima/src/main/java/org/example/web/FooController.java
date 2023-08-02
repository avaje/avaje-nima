package org.example.web;

import io.avaje.http.api.*;
import org.example.api.Foo;


@Valid(groups = {Default.class, Object.class})
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

  @Valid(groups = {Default.class, Object.class})
  @Produces("text/plain")
  @Post
  String perform(Foo foo) {
    return "ok";
  }

}
