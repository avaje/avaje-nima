package io.avaje.nima.provider;

import io.avaje.inject.BeanScopeBuilder;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.WebServerConfig;
import io.helidon.webserver.http.HttpRouting;

/** Provides defaults for Nima HttpRouting Builder and WebServerConfig Builder */
public class DefaultConfigProvider implements io.avaje.inject.spi.Plugin {

  @Override
  public Class<?>[] provides() {
    return new Class[] {WebServerConfig.Builder.class, HttpRouting.Builder.class};
  }

  @Override
  public void apply(BeanScopeBuilder builder) {
    builder.provideDefault(WebServerConfig.Builder.class, WebServer::builder);
    builder.provideDefault(HttpRouting.Builder.class, HttpRouting::builder);
  }
}
