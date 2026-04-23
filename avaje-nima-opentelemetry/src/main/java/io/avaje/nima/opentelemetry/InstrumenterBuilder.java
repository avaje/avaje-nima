package io.avaje.nima.opentelemetry;

import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.api.incubator.builder.internal.DefaultHttpServerInstrumenterBuilder;
import io.opentelemetry.instrumentation.api.instrumenter.AttributesExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.opentelemetry.instrumentation.api.instrumenter.SpanNameExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.SpanStatusExtractor;
import io.opentelemetry.instrumentation.api.semconv.http.HttpServerAttributesExtractorBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * Internal builder for the Helidon OTel instrumenter.
 */
final class InstrumenterBuilder {

  private static final String INSTRUMENTATION_NAME = "io.avaje.nima.helidon";

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
    DefaultHttpServerInstrumenterBuilder<ServerRequest, ServerResponse> builder =
      DefaultHttpServerInstrumenterBuilder.create(
        INSTRUMENTATION_NAME,
        openTelemetry,
        new HelidonAttributesGetter(),
        new HelidonRequestGetter());
    for (var extractor : attributesExtractors) {
      builder.addAttributesExtractor(extractor);
    }
    if (capturedRequestHeaders != null) {
      builder.setCapturedRequestHeaders(capturedRequestHeaders);
    }
    if (capturedResponseHeaders != null) {
      builder.setCapturedResponseHeaders(capturedResponseHeaders);
    }
    if (knownMethods != null) {
      builder.setKnownMethods(knownMethods);
    }
    if (spanNameExtractorCustomizer != null) {
      builder.setSpanNameExtractorCustomizer(spanNameExtractorCustomizer);
    }
    if (spanStatusExtractorCustomizer != null) {
      builder.setSpanStatusExtractorCustomizer(spanStatusExtractorCustomizer);
    }
    return builder.build();
  }
}
