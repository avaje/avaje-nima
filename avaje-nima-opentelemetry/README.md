# avaje-nima-opentelemetry

Adds an OpenTelemetry `Filter` for HTTP request tracing in `avaje-nima` applications.

This module creates the Helidon filter only. It expects an externally provided
`OpenTelemetry` instance and does **not** build or configure the OpenTelemetry SDK.

## Maven dependency

```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-nima-opentelemetry</artifactId>
  <version>${avaje.nima.version}</version>
</dependency>
```

If the application uses `module-info.java`, also add:

```java
requires io.avaje.nima.opentelemetry;
```

## Basic usage

```java
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

If the application already provides `OpenTelemetry` via autoconfigure, manual SDK
wiring, or another helper, use that bean instead of `GlobalOpenTelemetry.get()`.

## avaje-metrics OpenTelemetry publishing

For applications using `avaje-metrics-otel`, expose the `OpenTelemetry` instance built
by `MetricsOpenTelemetry` and reuse it for the filter:

```java
import io.avaje.config.Config;
import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.metrics.Metrics;
import io.avaje.metrics.otel.MetricsOpenTelemetry;
import io.avaje.nima.opentelemetry.NimaOtelFilter;
import io.helidon.webserver.http.Filter;
import io.opentelemetry.api.OpenTelemetry;

@Factory
final class OpenTelemetryConfig {

  @Bean
  OpenTelemetry openTelemetry() {
    Metrics.registry().registerJvmCoreMetrics();

    if (!Config.enabled("opentelemetry.publish", false)) {
      return OpenTelemetry.noop();
    }

    return MetricsOpenTelemetry.builder()
      .endpoint(Config.get("opentelemetry.endpoint"))
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

Set `opentelemetry.publish: false` in test configuration so tests use
`OpenTelemetry.noop()` and do not publish to a collector.

## Full guide

For the full step-by-step setup guide, including avaje-metrics integration and optional
filter customization, see
[docs/guides/add-open-telemetry-filter.md](../docs/guides/add-open-telemetry-filter.md).
