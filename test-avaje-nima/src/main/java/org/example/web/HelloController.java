package org.example.web;

import io.avaje.applog.AppLog;
import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Produces;
import io.avaje.inject.PreDestroy;
import io.avaje.jsonb.Json;
import io.avaje.metrics.Metrics;
import org.example.service.HelloService;

@Controller
public class HelloController {

  private final HelloService helloService;

  HelloController(HelloService helloService) {
    this.helloService = helloService;
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
    System.Logger log = AppLog.getLogger(HelloController.class);
    log.log(System.Logger.Level.INFO, "Hello one here \n again");
    return new Something(52, "Asdasd");
  }

  @Get("/metrics")
  String asJson() {
    String asJson = Metrics.collectAsJson().asJson();
    return asJson;
  }

  @Produces("text/plain")
  @Get("ithrow")
  String ithrow() {
    helloService.ithrow();
    return "hi";
  }

  @Json
  public record Something(int id, String name) {

  }
}
