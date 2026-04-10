package ${package}.web;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Path;
import io.avaje.http.api.Produces;
import ${package}.model.GreetingResponse;

@Controller
@Path("/hi")
public class HelloController {

  /** GET /hi  →  "hi" as text/plain */
  @Produces("text/plain")
  @Get
  String hi() {
    return "hi";
  }

  /** GET /hi/data  →  GreetingResponse as JSON */
  @Get("/data")
  GreetingResponse data() {
    return new GreetingResponse("hello from avaje-nima", System.currentTimeMillis());
  }
}
