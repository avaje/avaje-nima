package io.avaje.nima;

import io.avaje.inject.BeanScope;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.WebServerConfig;

public interface Nima {

  static Nima builder() {
    return new DNima();
  }

  Nima port(int port);

  Nima configure(BeanScope beanScope);

  Nima configure(WebServerConfig.Builder builder);

  Nima maxConcurrentRequests(int maxConcurrentRequests);

  Nima maxTcpConnections(int maxTcpConnections);

  Nima maxPayloadSize(long maxPayloadSize);

  Nima shutdownGraceMillis(long shutdownGraceMillis);

  /**
   * Register a Runnable to run on shutdown of the server with ordering.
   * <p>
   * The callbacks are executed with order from low to high (0 means run first).
   * <p>
   * This will execute after the server has deemed there are no active requests.
   *
   * @param callback The lifecycle callback function
   * @param order    The relative order to execute with 0 meaning run first
   */
  Nima register(AppLifecycle.Callback callback, int order);

  /**
   * Set if the default health endpoints should be included (defaults to true).
   * <p>
   * Defaults to true and add the following 2 routes to the web server that respond
   * based on the {@link AppLifecycle}.
   * <pre>{@code
   *
   *   /health/liveness
   *   /health/readiness
   *
   * }</pre>
   * @param health Set false to not include the health endpoints
   */
  Nima health(boolean health);

  WebServer build();

}
