package org.example.config;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.helidon.webserver.WebServerConfig;
import io.helidon.webserver.http.HttpRouting;

@Factory
class ServerConfig {

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
}
