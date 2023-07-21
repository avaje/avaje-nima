package io.avaje.nima;

import io.avaje.config.Config;
import io.avaje.inject.BeanScope;
import io.helidon.nima.webserver.WebServer;
import io.helidon.nima.webserver.WebServerConfig;
import io.helidon.nima.webserver.http.HttpRouting;
import io.helidon.nima.webserver.http.HttpService;

public interface Nima {

  static Nima builder() {
    return new DNima();
  }

  Nima port(int port);

  Nima configure(BeanScope beanScope);

  Nima configure(WebServerConfig.Builder builder);

  WebServer build();


  final class DNima implements Nima {

    private BeanScope beanScope;
    private WebServerConfig.Builder builder;
    private int port = Config.getInt("avaje.nima.port", 8080);

    @Override
    public Nima configure(WebServerConfig.Builder builder) {
      this.builder = builder;
      return this;
    }

    @Override
    public Nima configure(BeanScope beanScope) {
      this.beanScope = beanScope;
      return this;
    }

    @Override
    public Nima port(int port) {
      this.port = port;
      return this;
    }

    /**
     * Build the WebServer without starting it.
     */
    public WebServer build() {
      if (beanScope == null) {
        beanScope = BeanScope.builder().build();
      }
      HttpRouting.Builder routeBuilder = beanScope.getOptional(HttpRouting.Builder.class)
        .orElse(HttpRouting.builder());

      for (final HttpService httpService : beanScope.list(HttpService.class)) {
        httpService.routing(routeBuilder);
      }
      if (builder == null) {
        builder = beanScope.getOptional(WebServerConfig.Builder.class).orElse(WebServer.builder());
      }
      builder.addRouting(routeBuilder.build());
      builder.port(port);
      return builder.build();
    }
  }
}
