package io.avaje.nima;

import io.avaje.config.Config;
import io.avaje.inject.BeanScope;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.WebServerConfig;
import io.helidon.webserver.http.HttpFeature;
import io.helidon.webserver.http.HttpRouting;

import java.time.Duration;

final class DNima implements Nima {

  private BeanScope beanScope;
  private WebServerConfig.Builder configBuilder;
  private int port = Config.getInt("server.port", 8080);

  private int maxConcurrentRequests = Config.getInt("server.maxConcurrentRequests", 0);
  private int maxTcpConnections = Config.getInt("server.maxTcpConnections", 0);
  private long maxPayloadSize = Config.getLong("server.maxPayloadSize", 0);
  //private boolean shutdownHook = Config.getBool("server.shutdownHook", false);
  private long shutdownGraceMillis = Config.getInt("server.shutdownGraceMillis", 0);

  private boolean health = Config.getBool("server.health", true);

  private final DLifecycle lifecycle = new DLifecycle();

  @Override
  public Nima configure(WebServerConfig.Builder configBuilder) {
    this.configBuilder = configBuilder;
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

  @Override
  public Nima maxConcurrentRequests(int maxConcurrentRequests) {
    this.maxConcurrentRequests = maxConcurrentRequests;
    return this;
  }

  @Override
  public Nima maxTcpConnections(int maxTcpConnections) {
    this.maxTcpConnections = maxTcpConnections;
    return this;
  }

  @Override
  public Nima maxPayloadSize(long maxPayloadSize) {
    this.maxPayloadSize = maxPayloadSize;
    return this;
  }

  @Override
  public Nima shutdownGraceMillis(long shutdownGraceMillis) {
    this.shutdownGraceMillis = shutdownGraceMillis;
    return this;
  }

  @Override
  public Nima health(boolean health) {
    this.health = health;
    return this;
  }

  @Override
  public Nima register(AppLifecycle.Callback callback, int order) {
    lifecycle.register(callback, order);
    return this;
  }

  @Override
  public Nima preStart(Runnable preStartAction, int order) {
    lifecycle.preStart(preStartAction, order);
    return this;
  }

  @Override
  public Nima postStart(Runnable postStartAction, int order) {
    lifecycle.postStart(postStartAction, order);
    return this;
  }

  @Override
  public Nima preStop(Runnable preStopAction, int order) {
    lifecycle.preStop(preStopAction, order);
    return this;
  }

  @Override
  public Nima postStop(Runnable postStopAction, int order) {
    lifecycle.postStop(postStopAction, order);
    return this;
  }

  /**
   * Build the WebServer without starting it.
   */
  @Override
  public WebServer build() {
    if (beanScope == null) {
      final var scopeBuilder = BeanScope.builder();
      if (configBuilder != null) {
        scopeBuilder.bean(AppLifecycle.class, lifecycle);
        scopeBuilder.bean(WebServerConfig.Builder.class, configBuilder);
      }
      beanScope = scopeBuilder.build();
    }
    lifecycle.postStop(beanScope::close, 1000);

    final HttpRouting.Builder routeBuilder = beanScope.get(HttpRouting.Builder.class);
    if (health) {
      HealthPlugin.apply(lifecycle, routeBuilder);
    }

    beanScope.list(HttpFeature.class).forEach(routeBuilder::addFeature);
    if (configBuilder == null) {
      configBuilder = beanScope.get(WebServerConfig.Builder.class);
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
    return new DWebServer(configBuilder.build(), lifecycle);
  }

}
