package io.avaje.nima;

import io.avaje.inject.BeanScope;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.WebServerConfig;

/**
 * Nima is used to bootstrap the application.
 *
 * <pre>{@code
 *
 *  var webServer = Nima.builder()
 *    .port(8082)
 *    .build();
 *
 *  webServer.start();
 *
 * }</pre>
 */
public interface Nima {

  /**
   * Create the Nima builder.
   *
   * <pre>{@code
   *
   *  var webServer = Nima.builder()
   *    .port(8082)
   *    .build();
   *
   *  webServer.start();
   *
   * }</pre>
   */
  static Builder builder() {
    return new DNimaBuilder();
  }

  /**
   * Return the BeanScope.
   */
  BeanScope beanScope();

  /**
   * Return the Helidon WebServer.
   */
  WebServer server();

  /**
   * Start the Helidon WebServer and return it.
   */
  WebServer start();

  /**
   * Builder for Nima.
   */
  interface Builder {

    /**
     * Set the port to use. The default port is 8080.
     */
    Builder port(int port);

    /**
     * Configure using the explicit BeanScope.
     */
    Builder configure(BeanScope beanScope);

    /**
     * Configure using an explicit Helidon WebServerConfig.Builder.
     */
    Builder configure(WebServerConfig.Builder builder);

    /**
     * Set the max current requests.
     */
    Builder maxConcurrentRequests(int maxConcurrentRequests);

    /**
     * Set the max tcp connections.
     */
    Builder maxTcpConnections(int maxTcpConnections);

    /**
     * Set the allowed max payload size.
     */
    Builder maxPayloadSize(long maxPayloadSize);

    /**
     * Set the maximum graceful shutdown time.
     */
    Builder shutdownGraceMillis(long shutdownGraceMillis);

    /**
     * Register a Runnable to run on shutdown of the server with ordering.
     *
     * <p>The callbacks are executed with order from low to high (0 means run first).
     *
     * <p>This will execute after the server has deemed there are no active requests.
     *
     * @param callback The lifecycle callback function
     * @param order    The relative order to execute with 0 meaning run first
     */
    Builder register(AppLifecycle.Callback callback, int order);

    /**
     * Register a preStart lifecycle callback.
     */
    Builder preStart(Runnable preStartAction, int order);

    /**
     * Register a postStart lifecycle callback.
     */
    Builder postStart(Runnable postStartAction, int order);

    /**
     * Register a preStop lifecycle callback.
     */
    Builder preStop(Runnable preStopAction, int order);

    /**
     * Register a postStop lifecycle callback.
     */
    Builder postStop(Runnable postStopAction, int order);

    /**
     * Set if the default health endpoints should be included (defaults to true).
     *
     * <p>Defaults to true and add the following 2 routes to the web server that respond based on
     * the {@link AppLifecycle}.
     *
     * <pre>{@code
     * /health/liveness
     * /health/readiness
     *
     * }</pre>
     *
     * @param health Set false to not include the health endpoints
     */
    Builder health(boolean health);

    /**
     * Build and return the Nima instance.
     * <p>
     * The Nima instance contains the underlying Helidon WebServer and BeanScope
     * and the WebServer has not yet been started.
     *
     * <pre>{@code
     *
     *  var webServer = Nima.builder()
     *    .port(8082)
     *    .build();
     *
     *  webServer.start();
     *
     * }</pre>
     */
    Nima build();
  }

}
