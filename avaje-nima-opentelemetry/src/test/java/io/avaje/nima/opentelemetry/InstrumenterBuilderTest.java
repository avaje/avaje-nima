package io.avaje.nima.opentelemetry;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.instrumentation.api.instrumenter.AttributesExtractor;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class InstrumenterBuilderTest {

  @Test
  void buildSupportsStableHttpServerComposition() {
    var spanNameCustomized = new AtomicBoolean();
    var spanStatusCustomized = new AtomicBoolean();

    var instrumenter = new InstrumenterBuilder()
      .addAttributesExtractor(AttributesExtractor.constant(AttributeKey.stringKey("extra"), "value"))
      .setCapturedRequestHeaders(List.of("x-request-id"))
      .setCapturedResponseHeaders(List.of("content-type"))
      .setKnownMethods(List.of("GET", "PROPFIND"))
      .setSpanNameExtractorCustomizer(extractor -> {
        spanNameCustomized.set(true);
        return extractor;
      })
      .setSpanStatusExtractorCustomizer(extractor -> {
        spanStatusCustomized.set(true);
        return extractor;
      })
      .build(OpenTelemetry.noop());

    assertThat(instrumenter).isNotNull();
    assertThat(spanNameCustomized).isTrue();
    assertThat(spanStatusCustomized).isTrue();
  }
}
