package org.example.config;

import io.avaje.applog.AppLog;
import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.helidon.webserver.WebServerConfig;
import io.helidon.webserver.http.HttpRouting;

@Factory
class ServerConfig {

  static final System.Logger log = AppLog.getLogger(ServerConfig.class);

  @Bean
  void server(WebServerConfig.Builder serverBuilder) {
    int port = serverBuilder.port();
    log.log(System.Logger.Level.INFO, "got port: {0}", port);
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
