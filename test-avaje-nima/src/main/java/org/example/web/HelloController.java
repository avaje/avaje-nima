package org.example.web;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Produces;
import io.avaje.inject.PreDestroy;
import io.avaje.jsonb.Json;
import io.avaje.metrics.Metrics;

@Controller
public class HelloController {

  HelloController() {
    Metrics.jvmMetrics().registerJvmMetrics();
  }

  @Produces("text/plain")
  @Get("/")
  String hello() {
    return "hello world";
  }

  @PreDestroy
  void close() {
    System.out.println("HelloController closing ... ");
  }

  @Get("/one")
  Something one() {
    return new Something(52, "Asdasd");
  }

  @Get("/metrics")
  String asJson() {
    return Metrics.collectAsJson().asJson();
  }

  @Json
  public record Something(int id, String name) {

  }
}
