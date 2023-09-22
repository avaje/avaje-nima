package io.avaje.nima;

import io.helidon.http.Http;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

/**
 * Health plugin with liveness and readiness support based on
 * the application lifecycle support.
 */
final class HealthPlugin {

  private static final Http.HeaderName contentType = Http.HeaderNames.CONTENT_TYPE;

  private final AppLifecycle lifecycle;

  static void apply(AppLifecycle lifecycle, HttpRouting.Builder routeBuilder) {
    new HealthPlugin(lifecycle, routeBuilder);
  }

  private HealthPlugin(AppLifecycle lifecycle, HttpRouting.Builder routeBuilder) {
    this.lifecycle = lifecycle;
    routeBuilder.get("/health/liveness", this::liveness);
    routeBuilder.get("/health/readiness", this::readiness);
    routeBuilder.get("/health", this::readiness);
  }


  private static void send(ServerResponse res, int status, String ok) {
    res.status(status).header(contentType, "text/plain").send(ok);
  }

  private void readiness(ServerRequest req, ServerResponse res) {
    if (lifecycle.isReady()) {
      send(res, 200, "ok");
    } else {
      send(res, 500, "not-ready");
    }
  }

  private void liveness(ServerRequest req, ServerResponse res) {
    if (lifecycle.isAlive()) {
      send(res, 200, "ok");
    } else {
      send(res, 500, "not-ready");
    }
  }
}
