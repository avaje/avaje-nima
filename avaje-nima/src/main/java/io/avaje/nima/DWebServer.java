package io.avaje.nima;

import io.helidon.common.context.Context;
import io.helidon.common.tls.Tls;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.WebServerConfig;

/**
 * Wraps the underlying Helidon WebServer with JVM shutdown hook and shutdown lifecycle.
 */
final class DWebServer implements WebServer {

  private final WebServer delegate;
  private final DLifecycle lifecycle;

  private final Hook shutdownHook;

  DWebServer(WebServer delegate, DLifecycle lifecycle) {
    this.delegate = delegate;
    this.lifecycle = lifecycle;
    if (delegate.port() == 0) {
      // random port for testing so explicit shutdown expected
      this.shutdownHook = null;
    } else {
      this.shutdownHook = new Hook();
      Runtime.getRuntime().addShutdownHook(shutdownHook);
    }
  }

  private void removeShutdownHook() {
    if (shutdownHook != null) {
      Runtime.getRuntime().removeShutdownHook(shutdownHook);
    }
  }

  private void shutdown() {
    lifecycle.stop(delegate);
  }

  @Override
  public WebServer start() {
    lifecycle.start(delegate);
    return this;
  }

  /**
   * Explicit shutdown invoked.
   */
  @Override
  public WebServer stop() {
    removeShutdownHook();
    shutdown();
    return this;
  }

  @Override
  public boolean isRunning() {
    return delegate.isRunning();
  }

  @Override
  public int port(String socketName) {
    return delegate.port(socketName);
  }

  @Override
  public Context context() {
    return delegate.context();
  }

  @Override
  public boolean hasTls(String socketName) {
    return delegate.hasTls(socketName);
  }

  @Override
  public void reloadTls(String socketName, Tls tls) {
    delegate.reloadTls(socketName, tls);
  }

  @Override
  public WebServerConfig prototype() {
    return delegate.prototype();
  }

  private class Hook extends Thread {
    Hook() {
      super("nima-stop");
    }
    @Override
    public void run() {
      shutdown();
    }
  }
}
