package org.example.web;

import org.example.api.Foo;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Path;
import io.avaje.http.api.Post;
import io.avaje.http.api.Produces;
import io.avaje.http.api.Valid;
import io.avaje.validation.groups.Default;


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
