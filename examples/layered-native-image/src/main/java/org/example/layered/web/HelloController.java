package org.example.layered.web;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Path;
import io.avaje.http.api.Produces;
import org.example.layered.model.GreetingResponse;

@Controller
@Path("/hi")
public class HelloController {

  @Produces("text/plain")
  @Get
  String hi() {
    return "hi";
  }

  @Get("/data")
  GreetingResponse data() {
    return new GreetingResponse("hello from avaje-nima layered demo", System.currentTimeMillis());
  }
}
