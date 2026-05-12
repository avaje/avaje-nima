package io.avaje.nima.opentelemetry;

import io.avaje.config.Config;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.OpenTelemetrySdkBuilder;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;

import java.time.Duration;

import static java.util.Objects.requireNonNull;

/**
 * Helper to configure and build OpenTelemetry SDK.
 * <p>
 * Note that it is usually important to use {@link Builder#buildAndRegisterGlobal()} to ensure the SDK
 * is registered as the global instance.
 * </p>
 *
 * <pre>{@code
 *
 *   @Factory
 *   final class OpenTelemetryConfig {
 *
 *     @Bean
 *     OpenTelemetry openTelemetry() {
 *         return NimaOpenTelemetry.builder()
 *                 .enabled(Config.enabled("otel.export.enabled", true))
 *                 .endpoint(Config.get("otel.export.endpoint"))
 *                 .serviceName(Config.get("otel.service.name", "unknown"))
 *                 .buildAndRegisterGlobal();
 *     }
 *
 *     @Bean
 *     Filter filter(OpenTelemetry openTelemetry) {
 *         return NimaOtelFilter.builder(openTelemetry)
 *                 .excludeHealthPaths(true)
 *                 .build();
 *     }
 *   }
 *
 * }</pre>
 */
public final class NimaOpenTelemetry {

  /**
   * Return a new builder for configuring and building OpenTelemetry.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder for OpenTelemetry SDK.
   */
  public static class Builder {

    private boolean enabled = Config.enabled("otel.export.enabled", true);
    private boolean includeTrace = Config.enabled("otel.trace.enabled", true);
    private boolean includeMeter = Config.enabled("otel.meter.enabled", true);

    private String endpoint = Config.getNullable("otel.export.endpoint");
    private String serviceName = Config.getNullable("otel.service.name");
    private ContextPropagators contextPropagators;
    private SpanExporter spanExporter;
    private MetricExporter metricExporter;
    private Duration traceInterval = Duration.ofSeconds(5);;
    private Duration meterInterval = Duration.ofSeconds(60);

    private Builder() {}

    /** When true use NOOP OpenTelemetry. */
    public Builder enabled(boolean enabled) {
      this.enabled = enabled;
      return this;
    }

    /** When true includes traces (defaults to true). */
    public Builder includeTrace(boolean includeTrace) {
      this.includeTrace = includeTrace;
      return this;
    }

    /** When true includes meters (defaults to true). */
    public Builder includeMeter(boolean includeMeter) {
      this.includeMeter = includeMeter;
      return this;
    }

    /** Set the endpoint to export to. */
    public Builder endpoint(String endpoint) {
      this.endpoint = requireNonNull(endpoint);
      return this;
    }

    /** Set the service name. */
    public Builder serviceName(String serviceName) {
      this.serviceName = requireNonNull(serviceName);
      return this;
    }

    /** Set ContextPropagators (defaults to W3CTraceContextPropagator). */
    public Builder contextPropagators(ContextPropagators contextPropagators) {
      this.contextPropagators = contextPropagators;
      return this;
    }

    /** Set the spanExporter. Defaults to OtlpGrpcSpanExporter. */
    public Builder spanExporter(SpanExporter spanExporter) {
      this.spanExporter = spanExporter;
      return this;
    }

    /** Set the metricExporter. Defaults to OtlpGrpcMetricExporter. */
    public Builder metricExporter(MetricExporter metricExporter) {
      this.metricExporter = metricExporter;
      return this;
    }

    /** Set the traceInterval. Defaults to 5 seconds. */
    public Builder traceInterval(Duration traceInterval) {
      this.traceInterval = requireNonNull(traceInterval);
      return this;
    }

    /** Set the meterInterval. Defaults to 60 seconds. */
    public Builder meterInterval(Duration meterInterval) {
      this.meterInterval = requireNonNull(meterInterval);
      return this;
    }

    /**
     * Build and return OpenTelemetry.
     */
    public OpenTelemetry build() {
      if (!enabled) return OpenTelemetry.noop();
      return builder().build();
    }

    /**
     * Build and return OpenTelemetry registering it as the global instance.
     * <p>
     * An exception will be thrown if this method is attempted to be called
     * multiple times in the lifecycle of an application.
     */
    public OpenTelemetry buildAndRegisterGlobal() {
      if (!enabled) return OpenTelemetry.noop();
      return builder().buildAndRegisterGlobal();
    }

    private OpenTelemetrySdkBuilder builder() {
      requireNonNull(endpoint, "endpoint must be set");
      requireNonNull(serviceName, "serviceName must be set");
      var builder = OpenTelemetrySdk.builder().setPropagators(provideContextPropagators());
      var serviceName = serviceName();
      if (includeTrace) {
        builder.setTracerProvider(sdkTracerProvider(serviceName));
      }
      if (includeMeter) {
        builder.setMeterProvider(sdkMeterProvider(serviceName));
      }
      return builder;
    }

    private Resource serviceName() {
      return Resource.builder()
        .put("service.name", serviceName)
        .build();
    }

    private SdkTracerProvider sdkTracerProvider(Resource serviceName) {
      var batchProcessor = BatchSpanProcessor.builder(spanExporter())
        .setScheduleDelay(traceInterval)
        .build();

      return SdkTracerProvider.builder()
        .addResource(serviceName)
        .addSpanProcessor(batchProcessor)
        .build();
    }

    private SdkMeterProvider sdkMeterProvider(Resource serviceName) {
      return SdkMeterProvider.builder()
          .addResource(serviceName)
          .registerMetricReader(metricReader(metricExporter()))
          .build();
    }

    private MetricReader metricReader(MetricExporter metricExporter) {
      return PeriodicMetricReader.builder(metricExporter)
        .setInterval(meterInterval)
        .build();
    }

    private SpanExporter spanExporter() {
      if (spanExporter != null) return spanExporter;
      return OtlpGrpcSpanExporter.builder()
        .setEndpoint(endpoint)
        .build();
    }

    private MetricExporter metricExporter() {
      if (metricExporter != null) return metricExporter;
      return OtlpGrpcMetricExporter.builder()
        .setEndpoint(endpoint)
        .build();
    }

    private ContextPropagators provideContextPropagators() {
      if (contextPropagators != null) return contextPropagators;
      return ContextPropagators.create(W3CTraceContextPropagator.getInstance());
    }
  }
}
