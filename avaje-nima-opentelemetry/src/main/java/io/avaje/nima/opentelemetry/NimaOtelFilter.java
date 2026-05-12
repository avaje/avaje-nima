package io.avaje.nima.opentelemetry;

import io.avaje.config.Config;
import io.helidon.webserver.http.Filter;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.api.instrumenter.AttributesExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.SpanNameExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.SpanStatusExtractor;

import java.util.*;
import java.util.function.UnaryOperator;

/**
 * Builder for creating an OpenTelemetry {@link Filter} for Helidon Nima.
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
public final class NimaOtelFilter {


  private NimaOtelFilter() {
  }

  /**
   * Returns a new builder.
   *
   * @param openTelemetry The openTelemetry SDK that the filter will use.
   */
  public static Builder builder(OpenTelemetry openTelemetry) {
    return new Builder(openTelemetry);
  }

  /**
   * Builder for {@link NimaOtelFilter}.
   */
  public static final class Builder {

    private final InstrumenterBuilder instrumenterBuilder = new InstrumenterBuilder();
    private final List<String> excludedPaths = new ArrayList<>();
    private final OpenTelemetry openTelemetry;
    private boolean excludeHealthPaths = Config.enabled("otel.health.exclude", true);

    private Builder(OpenTelemetry openTelemetry) {
      this.openTelemetry = Objects.requireNonNull(openTelemetry);
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
      var instrumenter = instrumenterBuilder.build(openTelemetry);
      List<String> allExcluded = new ArrayList<>(excludedPaths);
      if (excludeHealthPaths) {
        allExcluded.add("/health");
      }
      return new OpenTelemetryFilter(instrumenter, List.copyOf(allExcluded));
    }
  }
}
