# Guide: Add OpenTelemetry Filter

## Purpose

This guide provides step-by-step instructions for adding OpenTelemetry HTTP request
tracing to an **avaje-nima** application using `avaje-nima-opentelemetry`.

When asked to *"add OpenTelemetry tracing"*, *"add an OTEL filter"*, *"trace HTTP
requests"*, or *"add `avaje-nima-opentelemetry`"* to an avaje-nima project, follow
these steps exactly.

---

## Overview

The HTTP tracing pattern has two moving parts:

| Bean | Purpose |
|---|---|
| `OpenTelemetry` | The application's existing OpenTelemetry instance |
| `Filter` | Helidon filter built with `NimaOtelFilter` to trace incoming HTTP requests |

`avaje-nima-opentelemetry` creates the Helidon filter only. It does **not** build or
configure the OpenTelemetry SDK for you.

The `OpenTelemetry` bean can come from:

- `GlobalOpenTelemetry.get()` when using the OpenTelemetry Java agent or another global setup
- OpenTelemetry autoconfiguration or manual SDK wiring already present in the application
- `MetricsOpenTelemetry` from `avaje-metrics-otel`, which is the common pattern when the
  application also publishes metrics via OpenTelemetry

---

## Step 1 — Add the dependency

Add `avaje-nima-opentelemetry` to `pom.xml`:

```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-nima-opentelemetry</artifactId>
  <version>${avaje.nima.version}</version>
</dependency>
```

If the project uses `module-info.java`, also add:

```java
requires io.avaje.nima.opentelemetry;
```

---

## Step 2 — Provide an `OpenTelemetry` bean

Create a factory class at:

```
src/main/java/<base-package>/configuration/OpenTelemetryConfig.java
```

Replace `<base-package>` with the application's root package.

For the minimal setup, use the global OpenTelemetry instance:

```java
package <base-package>.configuration;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;

@Factory
final class OpenTelemetryConfig {

  @Bean
  OpenTelemetry openTelemetry() {
    return GlobalOpenTelemetry.get();
  }
}
```

This is the recommended minimal example when the application is already using the
OpenTelemetry Java agent or another global setup.

> If the application already creates an `OpenTelemetry` instance via autoconfigure,
> manual SDK wiring, or another helper, expose that bean instead. Do **not** create a
> second OpenTelemetry instance just for the filter.

---

## Step 3 — Optional: build OpenTelemetry from avaje-metrics

When the application uses `avaje-metrics-otel`, expose the `OpenTelemetry` instance
created by `MetricsOpenTelemetry` and reuse it for `NimaOtelFilter`.

Add the metrics OpenTelemetry dependency:

```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-metrics-otel</artifactId>
  <version>${avaje.metrics.version}</version>
</dependency>
```

Then create the bean:

```java
package <base-package>.configuration;

import io.avaje.config.Config;
import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.metrics.Metrics;
import io.avaje.metrics.otel.MetricsOpenTelemetry;
import io.avaje.nima.opentelemetry.NimaOtelFilter;
import io.helidon.webserver.http.Filter;
import io.opentelemetry.api.OpenTelemetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Factory
final class OpenTelemetryConfig {

  private static final Logger log = LoggerFactory.getLogger(OpenTelemetryConfig.class);

  @Bean
  OpenTelemetry openTelemetry() {
    Metrics.registry().registerJvmCoreMetrics();

    if (!Config.enabled("opentelemetry.publish", false)) {
      log.info("No opentelemetry reporting");
      return OpenTelemetry.noop();
    }

    var endpoint = Config.get("opentelemetry.endpoint");
    log.info("Publishing opentelemetry metrics {}", endpoint);
    return MetricsOpenTelemetry.builder()
      .endpoint(endpoint)
      .serviceName("my-service")
      .buildAndRegisterGlobal();
  }

  @Bean
  Filter openTelemetryFilter(OpenTelemetry openTelemetry) {
    return NimaOtelFilter.builder(openTelemetry)
      .excludeHealthPaths(true)
      .excludePaths("/metrics")
      .build();
  }
}
```

Recommended configuration:

```yaml
# application.yaml or dynamic config
opentelemetry.publish: true
opentelemetry.endpoint: http://otel-collector:4317
```

For tests, disable publishing so integration tests do not try to contact a collector:

```yaml
# src/test/resources/application-test.yaml
opentelemetry.publish: false
```

If the project also uses avaje-metrics naming configuration, include the metrics
manifest in `src/main/resources/metrics.mf`:

```properties
timedMetricNaming: label-tag
```

This pattern still gives `NimaOtelFilter` the same `OpenTelemetry` bean; the difference
is that the bean also registers JVM metrics and configures OTLP publishing for the
service when `opentelemetry.publish` is enabled.

---

## Step 4 — Register `NimaOtelFilter`

Add the filter bean to the same factory class:

```java
package <base-package>.configuration;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.nima.opentelemetry.NimaOtelFilter;
import io.helidon.webserver.http.Filter;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;

@Factory
final class OpenTelemetryConfig {

  @Bean
  OpenTelemetry openTelemetry() {
    return GlobalOpenTelemetry.get();
  }

  @Bean
  Filter openTelemetryFilter(OpenTelemetry openTelemetry) {
    return NimaOtelFilter.builder(openTelemetry)
      .excludeHealthPaths(true)
      .excludePaths("/metrics")
      .build();
  }
}
```

Default behavior:

- `/health` is excluded when `excludeHealthPaths(true)` is used
- `excludePaths("/metrics")` excludes `/metrics` and any child path via prefix matching
- all traced requests produce normal OpenTelemetry server spans using the supplied
  `OpenTelemetry` instance

---

## Step 5 — Optional customization

`NimaOtelFilter` exposes a few useful customisation hooks:

- `excludePaths(...)` for additional endpoints such as `/ready` or `/internal`
- `setCapturedRequestHeaders(...)` to record selected request headers as span attributes
- `setCapturedResponseHeaders(...)` to record selected response headers
- `setKnownMethods(...)` when non-standard HTTP methods should be recognised
- `addAttributesExtractor(...)` to add app-specific span attributes

Example:

```java
package <base-package>.configuration;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.nima.opentelemetry.NimaOtelFilter;
import io.helidon.webserver.http.Filter;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.instrumentation.api.instrumenter.AttributesExtractor;

import java.util.List;

@Factory
final class OpenTelemetryConfig {

  @Bean
  OpenTelemetry openTelemetry() {
    return GlobalOpenTelemetry.get();
  }

  @Bean
  Filter openTelemetryFilter(OpenTelemetry openTelemetry) {
    return NimaOtelFilter.builder(openTelemetry)
      .excludeHealthPaths(true)
      .excludePaths("/metrics", "/ready")
      .setCapturedRequestHeaders(List.of("x-request-id"))
      .setCapturedResponseHeaders(List.of("content-type"))
      .setKnownMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE"))
      .addAttributesExtractor(
        AttributesExtractor.constant(
          AttributeKey.stringKey("deployment.environment"),
          "prod"))
      .build();
  }
}
```

> `setKnownMethods(...)` replaces the default recognised methods completely. Only call
> it when needed, and include every method the application should treat as a known HTTP
> method.

---

## Step 6 — Verify

1. Start the application with OpenTelemetry configured.
2. Send a request to a traced endpoint:

```bash
curl -i http://localhost:8080/
```

3. Confirm the request appears in the configured trace backend / collector.

If using the OpenTelemetry Java agent, make sure the JVM is started with the agent and
the normal OTEL environment variables or system properties are configured before
starting the application.

---

## Notes

- `avaje-nima-opentelemetry` handles **HTTP tracing only**. It does not configure
  exporters, collectors, resources, or the OpenTelemetry SDK.
- The filter traces incoming HTTP requests; traced timers and metrics are separate
  concerns.
- If the application also uses avaje-metrics with OpenTelemetry, you can supply that
  same `OpenTelemetry` bean to `NimaOtelFilter`. Use `OpenTelemetry.noop()` when
  publishing is disabled, especially in tests.
- The most likely place to add the factory class is a `telemetry`, `config`, or
  `infra` package near the application's other infrastructure beans.
