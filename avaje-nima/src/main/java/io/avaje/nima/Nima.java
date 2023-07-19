package io.avaje.nima;

import io.avaje.inject.BeanScope;
import io.helidon.nima.webserver.WebServer;
import io.helidon.nima.webserver.WebServerConfig;
import io.helidon.nima.webserver.http.HttpRouting;
import io.helidon.nima.webserver.http.HttpService;

public class Nima {

  private WebServerConfig.Builder builder;
  private WebServer webServer;

  public Nima configure(BeanScope beanScope) {
    HttpRouting.Builder routeBuilder = beanScope.getOptional(HttpRouting.Builder.class)
      .orElse(HttpRouting.builder());

    for (final HttpService httpService : beanScope.list(HttpService.class)) {
      httpService.routing(routeBuilder);
    }

    builder = beanScope.getOptional(WebServerConfig.Builder.class).orElse(WebServer.builder());
    builder.addRouting(routeBuilder.build());
    return this;
  }

  public Nima port(int port) {
    builder.port(port);
    return this;
  }

  public void start() {
    this.webServer = builder.build().start();
  }

  public void start(int port) {
    this.webServer = builder.port(port).build().start();
  }

  public int port() {
    return webServer.port();
  }

  public void stop() {
    webServer.stop();
  }
}
