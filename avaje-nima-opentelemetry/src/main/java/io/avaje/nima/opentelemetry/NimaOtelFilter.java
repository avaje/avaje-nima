package io.avaje.nima.opentelemetry;

import io.avaje.config.Config;
import io.helidon.webserver.http.Filter;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.instrumentation.api.instrumenter.AttributesExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.SpanNameExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.SpanStatusExtractor;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * Builder for creating an OpenTelemetry {@link Filter} for Helidon Nima.
 *
 * <p>Example — zero-config (reads config defaults, health paths excluded by default):
 * <pre>{@code
 * Filter filter = NimaOtelFilter.create();
 * }</pre>
 *
 * <p>Example — customised:
 * <pre>{@code
 * Filter filter = NimaOtelFilter.builder()
 *     .spanExportEnabled(false)
 *     .excludePaths("/metrics", "/ready")
 *     .setCapturedRequestHeaders(List.of("X-Request-ID"))
 *     .build();
 * }</pre>
 *
 * <p>Config keys:
 * <ul>
 *   <li>{@code otel.spanExport.enabled} — whether to export spans via OTLP (default: {@code true})</li>
 *   <li>{@code otel.health.exclude} — whether to exclude {@code /health} paths (default: {@code true})</li>
 * </ul>
 */
public final class NimaOtelFilter {

  private static final Logger log = LoggerFactory.getLogger(NimaOtelFilter.class);

  private NimaOtelFilter() {
  }

  /**
   * Creates a {@link Filter} with defaults, reading {@code otel.spanExport.enabled} from config.
   */
  public static Filter create() {
    return builder().build();
  }

  /**
   * Returns a new builder.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder for {@link NimaOtelFilter}.
   */
  public static final class Builder {

    private final InstrumenterBuilder instrumenterBuilder = new InstrumenterBuilder();
    private final List<String> excludedPaths = new ArrayList<>();
    private boolean spanExportEnabled = Config.enabled("otel.spanExport.enabled", true);
    private boolean excludeHealthPaths = Config.enabled("otel.health.exclude", true);
    private SpanExporter spanExporter;
    private OpenTelemetry openTelemetry;

    private Builder() {
    }

    /**
     * Whether to export traces via OTLP. Defaults to the {@code otel.spanExport.enabled} config
     * property (true if not set). Ignored if a custom {@link #spanExporter} or
     * {@link #openTelemetry} is provided.
     */
    public Builder spanExportEnabled(boolean spanExportEnabled) {
      this.spanExportEnabled = spanExportEnabled;
      return this;
    }

    /**
     * Provide a custom {@link SpanExporter}. When set, {@link #spanExportEnabled} is ignored.
     * Ignored if {@link #openTelemetry} is also set.
     */
    public Builder spanExporter(SpanExporter spanExporter) {
      this.spanExporter = spanExporter;
      return this;
    }

    /**
     * Provide a fully configured {@link OpenTelemetry} instance, bypassing all SDK setup
     * (tracerProvider, exporter, propagators).
     */
    public Builder openTelemetry(OpenTelemetry openTelemetry) {
      this.openTelemetry = openTelemetry;
      return this;
    }

    /**
     * Adds an {@link AttributesExtractor} executed after all default extractors.
     */
    public Builder addAttributesExtractor(AttributesExtractor<ServerRequest, ServerResponse> attributesExtractor) {
      instrumenterBuilder.addAttributesExtractor(attributesExtractor);
      return this;
    }

    /**
     * Configures HTTP request headers to capture as span attributes.
     */
    public Builder setCapturedRequestHeaders(Collection<String> requestHeaders) {
      instrumenterBuilder.setCapturedRequestHeaders(requestHeaders);
      return this;
    }

    /**
     * Configures HTTP response headers to capture as span attributes.
     */
    public Builder setCapturedResponseHeaders(Collection<String> responseHeaders) {
      instrumenterBuilder.setCapturedResponseHeaders(responseHeaders);
      return this;
    }

    /**
     * Configures recognized HTTP request methods.
     * <p><b>Note:</b> This overrides defaults completely; it does not supplement them.
     */
    public Builder setKnownMethods(Collection<String> knownMethods) {
      instrumenterBuilder.setKnownMethods(knownMethods);
      return this;
    }

    /**
     * Customizes the {@link SpanNameExtractor} by transforming the default instance.
     */
    public Builder setSpanNameExtractorCustomizer(UnaryOperator<SpanNameExtractor<ServerRequest>> customizer) {
      instrumenterBuilder.setSpanNameExtractorCustomizer(customizer);
      return this;
    }

    /**
     * Customizes the {@link SpanStatusExtractor} by transforming the default instance.
     */
    public Builder setSpanStatusExtractorCustomizer(UnaryOperator<SpanStatusExtractor<ServerRequest, ServerResponse>> customizer) {
      instrumenterBuilder.setSpanStatusExtractorCustomizer(customizer);
      return this;
    }

    /**
     * Whether to exclude {@code /health} paths from tracing. Defaults to the
     * {@code otel.health.exclude} config property (true if not set).
     */
    public Builder excludeHealthPaths(boolean exclude) {
      this.excludeHealthPaths = exclude;
      return this;
    }

    /**
     * Excludes paths with the given prefixes from tracing.
     * Uses {@code startsWith} matching — e.g. {@code "/metrics"} excludes
     * {@code /metrics}, {@code /metrics/jvm}, etc.
     */
    public Builder excludePaths(String... prefixes) {
      excludedPaths.addAll(Arrays.asList(prefixes));
      return this;
    }

    /**
     * Excludes paths with the given prefixes from tracing.
     * Uses {@code startsWith} matching.
     */
    public Builder excludePaths(Collection<String> prefixes) {
      excludedPaths.addAll(prefixes);
      return this;
    }

    /**
     * Builds the {@link Filter}.
     */
    public Filter build() {
      OpenTelemetry otel = resolveOpenTelemetry();
      var instrumenter = instrumenterBuilder.build(otel);
      List<String> allExcluded = new ArrayList<>(excludedPaths);
      if (excludeHealthPaths) {
        allExcluded.add("/health");
      }
      return new OpenTelemetryFilter(instrumenter, List.copyOf(allExcluded));
    }

    private OpenTelemetry resolveOpenTelemetry() {
      if (openTelemetry != null) {
        return openTelemetry;
      }
      SpanExporter exporter = resolveExporter();
      SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
        .addSpanProcessor(SimpleSpanProcessor.builder(exporter).build())
        .build();
      return OpenTelemetrySdk.builder()
        .setTracerProvider(tracerProvider)
        .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
        .build();
    }

    private SpanExporter resolveExporter() {
      if (spanExporter != null) {
        return spanExporter;
      }
      if (spanExportEnabled) {
        log.debug("exporting otel traces using OtlpGrpcSpanExporter");
        return OtlpGrpcSpanExporter.getDefault();
      }
      return NoopSpanExporter.getInstance();
    }
  }
}
