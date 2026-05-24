package io.avaje.nima.opentelemetry;

import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.api.instrumenter.AttributesExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.ContextCustomizer;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.opentelemetry.instrumentation.api.instrumenter.SpanNameExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.SpanStatusExtractor;
import io.opentelemetry.instrumentation.api.semconv.http.HttpServerAttributesExtractor;
import io.opentelemetry.instrumentation.api.semconv.http.HttpServerAttributesExtractorBuilder;
import io.opentelemetry.instrumentation.api.semconv.http.HttpServerMetrics;
import io.opentelemetry.instrumentation.api.semconv.http.HttpServerRoute;
import io.opentelemetry.instrumentation.api.semconv.http.HttpSpanNameExtractor;
import io.opentelemetry.instrumentation.api.semconv.http.HttpSpanStatusExtractor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * Internal builder for the Helidon OTel instrumenter.
 */
final class InstrumenterBuilder {

  private static final String INSTRUMENTATION_NAME = "io.avaje.nima.helidon";
  private static final String SCHEMA_URL = "https://opentelemetry.io/schemas/1.37.0";

  private final List<AttributesExtractor<ServerRequest, ServerResponse>> attributesExtractors = new ArrayList<>();
  private Collection<String> capturedRequestHeaders;
  private Collection<String> capturedResponseHeaders;
  private Collection<String> knownMethods;
  private UnaryOperator<SpanNameExtractor<ServerRequest>> spanNameExtractorCustomizer;
  private UnaryOperator<SpanStatusExtractor<ServerRequest, ServerResponse>> spanStatusExtractorCustomizer;

  InstrumenterBuilder() {
  }

  /**
   * Customizes the {@link SpanStatusExtractor} by transforming the default instance.
   */
  InstrumenterBuilder setSpanStatusExtractorCustomizer(UnaryOperator<SpanStatusExtractor<ServerRequest, ServerResponse>> spanStatusExtractorCustomizer) {
    this.spanStatusExtractorCustomizer = spanStatusExtractorCustomizer;
    return this;
  }

  /**
   * Adds an {@link AttributesExtractor} executed after all default extractors.
   */
  InstrumenterBuilder addAttributesExtractor(AttributesExtractor<ServerRequest, ServerResponse> attributesExtractor) {
    this.attributesExtractors.add(attributesExtractor);
    return this;
  }

  /**
   * Configures HTTP request headers to capture as span attributes.
   *
   * @param requestHeaders HTTP header names to capture.
   */
  InstrumenterBuilder setCapturedRequestHeaders(Collection<String> requestHeaders) {
    this.capturedRequestHeaders = requestHeaders;
    return this;
  }

  /**
   * Configures HTTP response headers to capture as span attributes.
   *
   * @param responseHeaders HTTP header names to capture.
   */
  InstrumenterBuilder setCapturedResponseHeaders(Collection<String> responseHeaders) {
    this.capturedResponseHeaders = responseHeaders;
    return this;
  }

  /**
   * Configures recognized HTTP request methods.
   *
   * <p>By default, recognizes methods from <a
   * href="https://www.rfc-editor.org/rfc/rfc9110.html#name-methods">RFC9110</a> and PATCH from <a
   * href="https://www.rfc-editor.org/rfc/rfc5789.html">RFC5789</a>.
   *
   * <p><b>Note:</b> This <b>overrides</b> defaults completely; it does not supplement them.
   *
   * @param knownMethods HTTP request methods to recognize.
   * @see HttpServerAttributesExtractorBuilder#setKnownMethods(Collection)
   */
  InstrumenterBuilder setKnownMethods(Collection<String> knownMethods) {
    this.knownMethods = knownMethods;
    return this;
  }

  /**
   * Customizes the {@link SpanNameExtractor} by transforming the default instance.
   */
  InstrumenterBuilder setSpanNameExtractorCustomizer(UnaryOperator<SpanNameExtractor<ServerRequest>> spanNameExtractorCustomizer) {
    this.spanNameExtractorCustomizer = spanNameExtractorCustomizer;
    return this;
  }

  Instrumenter<ServerRequest, ServerResponse> build(OpenTelemetry openTelemetry) {
    var attributesGetter = new HelidonAttributesGetter();
    var instrumenterBuilder = Instrumenter.<ServerRequest, ServerResponse>builder(
      openTelemetry,
      INSTRUMENTATION_NAME,
      spanNameExtractor(attributesGetter))
      .setSpanStatusExtractor(spanStatusExtractor(attributesGetter))
      .addAttributesExtractor(httpAttributesExtractor(attributesGetter))
      .addContextCustomizer(routeCustomizer(attributesGetter))
      .addOperationMetrics(HttpServerMetrics.get())
      .setSchemaUrl(SCHEMA_URL);
    for (var extractor : attributesExtractors) {
      instrumenterBuilder.addAttributesExtractor(extractor);
    }
    return instrumenterBuilder.buildServerInstrumenter(new HelidonRequestGetter());
  }

  private AttributesExtractor<ServerRequest, ServerResponse> httpAttributesExtractor(HelidonAttributesGetter attributesGetter) {
    var builder = HttpServerAttributesExtractor.builder(attributesGetter);
    if (capturedRequestHeaders != null) {
      builder.setCapturedRequestHeaders(capturedRequestHeaders);
    }
    if (capturedResponseHeaders != null) {
      builder.setCapturedResponseHeaders(capturedResponseHeaders);
    }
    if (knownMethods != null) {
      builder.setKnownMethods(knownMethods);
    }
    return builder.build();
  }

  private ContextCustomizer<ServerRequest> routeCustomizer(HelidonAttributesGetter attributesGetter) {
    var builder = HttpServerRoute.builder(attributesGetter);
    if (knownMethods != null) {
      builder.setKnownMethods(knownMethods);
    }
    return builder.build();
  }

  private SpanNameExtractor<ServerRequest> spanNameExtractor(HelidonAttributesGetter attributesGetter) {
    var builder = HttpSpanNameExtractor.builder(attributesGetter);
    if (knownMethods != null) {
      builder.setKnownMethods(knownMethods);
    }
    var extractor = builder.build();
    return spanNameExtractorCustomizer != null ? spanNameExtractorCustomizer.apply(extractor) : extractor;
  }

  private SpanStatusExtractor<ServerRequest, ServerResponse> spanStatusExtractor(HelidonAttributesGetter attributesGetter) {
    var extractor = HttpSpanStatusExtractor.create(attributesGetter);
    return spanStatusExtractorCustomizer != null ? spanStatusExtractorCustomizer.apply(extractor) : extractor;
  }
}
