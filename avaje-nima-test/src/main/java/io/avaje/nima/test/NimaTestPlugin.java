package io.avaje.nima.test;

import io.avaje.http.client.HttpClient;
import io.avaje.inject.BeanScope;
import io.avaje.inject.test.Plugin;
import io.avaje.nima.Nima;
import io.helidon.webserver.WebServer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * avaje-inject-test plugin that:
 * <p>
 * - Detects when a http client is being used in a test
 * - Starts Nima server on random port
 * - Creates the appropriate client for the port (to be injected into the test class)
 * - Shutdown the server on test completion (Plugin.Scope close)
 */
public final class NimaTestPlugin implements Plugin {

  private static final String AVAJE_HTTP_CLIENT = "io.avaje.http.api.Client";
  private static final String AVAJE_HTTP_PATH = "io.avaje.http.api.Path";

  /**
   * Return true if it's a http client this plugin supports.
   */
  @Override
  public boolean forType(Type type) {
    return HttpClient.class.equals(type)
      || HttpClient.Builder.class.equals(type)
      || WebServer.class.equals(type)
      || isHttpClientApi(type);
  }

  private boolean isHttpClientApi(Type rawtype) {
    if (!(rawtype instanceof Class<?> type) || !type.isInterface()) {
      return false;
    }
    for (Annotation annotation : type.getAnnotations()) {
      String name = annotation.annotationType().getName();
      if (AVAJE_HTTP_CLIENT.equals(name) || AVAJE_HTTP_PATH.equals(name)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Create a scope.
   * <p>
   * The scope will contain a server + client pair.
   */
  @Override
  public Scope createScope(BeanScope beanScope) {
    return new LocalScope(beanScope);
  }

  private static class LocalScope implements Plugin.Scope {

    private final WebServer server;
    private final HttpClient httpClient;
    private final String baseUrl;

    LocalScope(BeanScope beanScope) {
      this.server =
          beanScope
              .getOptional(Nima.class)
              .orElse(Nima.builder().configure(beanScope).port(0).build())
              .server();

      // get a HttpClientContext.Builder provided by dependency injection test scope or new one up
      server.start();
      int port = server.port();
      this.baseUrl = "http://localhost:" + port;
      this.httpClient = beanScope.getOptional(HttpClient.Builder.class)
        .orElse(HttpClient.builder())
        .configureWith(beanScope)
        .baseUrl(baseUrl)
        .build();
    }

    @Override
    public Object create(Type type) {
      if (HttpClient.class.equals(type)) {
        return httpClient;
      }
      if (HttpClient.Builder.class.equals(type)) {
        return HttpClient.builder().baseUrl(baseUrl);
      }
      if (WebServer.class.equals(type)) {
        return server;
      }
      return apiClient(type);
    }

    private Object apiClient(Type clientInterface) {
      return httpClient.create((Class<?>) clientInterface);
    }

    @Override
    public void close() {
      server.stop();
    }
  }
}
