package io.avaje.nima;

import io.avaje.inject.BeanScope;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.WebServerConfig;

public interface Nima {

  static Builder builder() {
    return new DNimaBuilder();
  }

  BeanScope beanScope();

  WebServer server();

  WebServer start();

  public interface Builder {

    Builder port(int port);

    Builder configure(BeanScope beanScope);

    Builder configure(WebServerConfig.Builder builder);

    Builder maxConcurrentRequests(int maxConcurrentRequests);

    Builder maxTcpConnections(int maxTcpConnections);

    Builder maxPayloadSize(long maxPayloadSize);

    Builder shutdownGraceMillis(long shutdownGraceMillis);

    /**
     * Register a Runnable to run on shutdown of the server with ordering.
     *
     * <p>The callbacks are executed with order from low to high (0 means run first).
     *
     * <p>This will execute after the server has deemed there are no active requests.
     *
     * @param callback The lifecycle callback function
     * @param order The relative order to execute with 0 meaning run first
     */
    Builder register(AppLifecycle.Callback callback, int order);

    /** Register a preStart lifecycle callback. */
    Builder preStart(Runnable preStartAction, int order);

    /** Register a postStart lifecycle callback. */
    Builder postStart(Runnable postStartAction, int order);

    /** Register a preStop lifecycle callback. */
    Builder preStop(Runnable preStopAction, int order);

    /** Register a postStop lifecycle callback. */
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

    Nima build();
  }

}
