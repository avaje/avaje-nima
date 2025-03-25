package org.example.config;

import io.avaje.inject.Bean;
import io.avaje.inject.BeanTypes;
import io.avaje.inject.Factory;
import io.avaje.inject.Secondary;
import io.helidon.webserver.WebServerConfig;
import io.helidon.webserver.http.*;
import jakarta.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Factory
class ServerConfig {

  private static final Logger log = LoggerFactory.getLogger(ServerConfig.class);

  @Bean
  void server(WebServerConfig.Builder serverBuilder) {
    int port = serverBuilder.port();
    System.out.println("got port: " + port);
  }

  @Bean
  void routing(HttpRouting.Builder routingBuilder) {
    routingBuilder.get("/health", (req, res) -> res.send("ok"));

    routingBuilder.error(Throwable.class, (req, res, exception) -> {
      System.out.println("asdad");
      exception.printStackTrace();
      res.send("General Barf");
    });
  }

  @Named("filterOne")
  @Bean
  Filter addFilter() {
    return (filterChain, routingRequest, routingResponse) -> {
      log.debug("Filter 1");
      filterChain.proceed();
    };
  }

  @Named("filterTwo")
  @Bean
  Filter addAnotherFilter() {
    return (filterChain, routingRequest, routingResponse) -> {
      log.debug("Filter 2");
      filterChain.proceed();
    };
  }
}
