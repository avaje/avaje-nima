package io.avaje.nima;

import io.avaje.config.Config;
import io.avaje.inject.BeanScope;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.WebServerConfig;
import io.helidon.webserver.http.Filter;
import io.helidon.webserver.http.HttpFeature;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.spi.ServerFeature;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

final class DNimaBuilder implements Nima.Builder {

  private BeanScope beanScope;
  private WebServerConfig.Builder configBuilder;
  private int port = Config.getInt("server.port", 8080);

  private int maxConcurrentRequests = Config.getInt("server.maxConcurrentRequests", 0);
  private int maxTcpConnections = Config.getInt("server.maxTcpConnections", 0);
  private long maxPayloadSize = Config.getLong("server.maxPayloadSize", 0);
  // private boolean shutdownHook = Config.getBool("server.shutdownHook", false);
  private long shutdownGraceMillis = Config.getInt("server.shutdownGraceMillis", 0);

  private boolean health = Config.getBool("server.health", true);

  private final DLifecycle lifecycle = new DLifecycle();
  private final List<Consumer<WebServerConfig.Builder>> configConsumers= new ArrayList<>();

  @Override
  public Nima.Builder configure(WebServerConfig.Builder configBuilder) {
    this.configBuilder = configBuilder;
    return this;
  }

  @Override
  public Nima.Builder configureServer(Consumer<WebServerConfig.Builder> configBuilder) {
    configConsumers.add(configBuilder);
    return this;
  }

  @Override
  public Nima.Builder configure(BeanScope beanScope) {
    this.beanScope = beanScope;
    return this;
  }

  @Override
  public Nima.Builder port(int port) {
    this.port = port;
    return this;
  }

  @Override
  public Nima.Builder maxConcurrentRequests(int maxConcurrentRequests) {
    this.maxConcurrentRequests = maxConcurrentRequests;
    return this;
  }

  @Override
  public Nima.Builder maxTcpConnections(int maxTcpConnections) {
    this.maxTcpConnections = maxTcpConnections;
    return this;
  }

  @Override
  public Nima.Builder maxPayloadSize(long maxPayloadSize) {
    this.maxPayloadSize = maxPayloadSize;
    return this;
  }

  @Override
  public Nima.Builder shutdownGraceMillis(long shutdownGraceMillis) {
    this.shutdownGraceMillis = shutdownGraceMillis;
    return this;
  }

  @Override
  public Nima.Builder health(boolean health) {
    this.health = health;
    return this;
  }

  @Override
  public Nima.Builder register(AppLifecycle.Callback callback, int order) {
    lifecycle.register(callback, order);
    return this;
  }

  @Override
  public Nima.Builder preStart(Runnable preStartAction, int order) {
    lifecycle.preStart(preStartAction, order);
    return this;
  }

  @Override
  public Nima.Builder postStart(Runnable postStartAction, int order) {
    lifecycle.postStart(postStartAction, order);
    return this;
  }

  @Override
  public Nima.Builder preStop(Runnable preStopAction, int order) {
    lifecycle.preStop(preStopAction, order);
    return this;
  }

  @Override
  public Nima.Builder postStop(Runnable postStopAction, int order) {
    lifecycle.postStop(postStopAction, order);
    return this;
  }

  /**
   * Build the WebServer without starting it.
   */
  @Override
  public Nima build() {
    // disable ebean (if used) from registering a shutdown hook
    // as we want ebean to shutdown last via avaje-inject PreDestroy
    System.setProperty("ebean.registerShutdownHook", "false");
    if (beanScope == null) {
      final var scopeBuilder = BeanScope.builder();
      if (configBuilder != null) {
        scopeBuilder.bean(AppLifecycle.class, lifecycle);
        scopeBuilder.bean(WebServerConfig.Builder.class, configBuilder);
      }
      beanScope = scopeBuilder.build();
    }
    // the DI BeanScope is shutdown after the web server
    lifecycle.postStop(beanScope::close, 1000);

    final HttpRouting.Builder routeBuilder = beanScope.get(HttpRouting.Builder.class);
    beanScope.list(Filter.class).forEach(routeBuilder::addFilter);
    beanScope.list(HttpFeature.class).forEach(routeBuilder::addFeature);
    if (configBuilder == null) {
      configBuilder = beanScope.get(WebServerConfig.Builder.class);
    }

    beanScope.list(ServerFeature.class).forEach(configBuilder::addFeature);
    if (health) {
      HealthPlugin.apply(lifecycle, routeBuilder);
    }
    if (maxConcurrentRequests > 0) {
      configBuilder.maxConcurrentRequests(maxConcurrentRequests);
    }
    if (maxTcpConnections > 0) {
      configBuilder.maxTcpConnections(maxTcpConnections);
    }
    if (maxPayloadSize > 0) {
      configBuilder.maxPayloadSize(maxPayloadSize);
    }
    if (shutdownGraceMillis > 0) {
      configBuilder.shutdownGracePeriod(Duration.ofMillis(shutdownGraceMillis));
    }
    configBuilder.shutdownHook(false);
    configBuilder.addRouting(routeBuilder);
    configBuilder.port(port);
    configConsumers.forEach(b -> b.accept(configBuilder));
    return new DNima(beanScope, new DWebServer(configBuilder.build(), lifecycle));
  }

  static final class DNima implements Nima {

    private final BeanScope beanScope;
    private final WebServer webServer;

    public DNima(BeanScope beanScope, WebServer webServer) {
      this.beanScope = beanScope;
      this.webServer = webServer;
    }

    @Override
    public BeanScope beanScope() {
      return beanScope;
    }

    @Override
    public WebServer server() {
      return webServer;
    }

    @Override
    public WebServer start() {
      return webServer.start();
    }
  }
}


