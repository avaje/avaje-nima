package org.example.web;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Path;
import io.avaje.http.api.Produces;
import io.avaje.http.api.QueryParam;

/**
 * Controller used to verify the treatment of characters like '+' in
 * path parameters versus query parameters (see avaje-jex PR #416).
 */
@Controller
@Path("/params")
public class ParamController {

  @Produces("text/plain")
  @Get("/path/{value}")
  String echoPath(String value) {
    return value;
  }

  @Produces("text/plain")
  @Get("/query")
  String echoQuery(@QueryParam("value") String value) {
    return value;
  }

  @Produces("text/plain")
  @Get("/query-default")
  String echoQueryDefault(@QueryParam("value") String value) {
    return value == null ? "default" : value;
  }
}
