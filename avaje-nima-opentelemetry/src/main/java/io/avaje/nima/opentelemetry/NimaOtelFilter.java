package io.avaje.nima.opentelemetry;

import io.helidon.webserver.http.Filter;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.api.instrumenter.AttributesExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.SpanNameExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.SpanStatusExtractor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

import static java.util.Objects.requireNonNull;

/**
 * Builder for creating an OpenTelemetry {@link Filter} for Helidon Nima.
 *
 * <p>Provide the {@link OpenTelemetry} instance from your application's telemetry setup. This module
 * creates the Helidon filter only and does not configure or build OpenTelemetry for you.
 *
 * <p>The supplied {@link OpenTelemetry} can come from the OpenTelemetry Java agent, OpenTelemetry
 * autoconfiguration, manual SDK wiring, or another helper such as
 * {@code io.avaje.metrics.otel.MetricsOpenTelemetry}.
 *
 * <pre>{@code
 *
 *   @Factory
 *   final class OpenTelemetryConfig {
 *
 *     @Bean
 *     OpenTelemetry openTelemetry() {
 *         return GlobalOpenTelemetry.get();
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
   * @param openTelemetry the externally provided OpenTelemetry instance the filter will use
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
    private boolean excludeHealthPaths = true;

    private Builder(OpenTelemetry openTelemetry) {
      this.openTelemetry = requireNonNull(openTelemetry);
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
     * Whether to exclude {@code /health} paths from tracing. Defaults to {@code true}.
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
