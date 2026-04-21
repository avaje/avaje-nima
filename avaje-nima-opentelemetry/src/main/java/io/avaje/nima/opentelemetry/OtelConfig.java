package io.avaje.nima.opentelemetry;

import io.avaje.config.Config;
import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.helidon.webserver.http.Filter;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.instrumentation.api.incubator.builder.internal.DefaultHttpServerInstrumenterBuilder;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * avaje-inject factory providing default OpenTelemetry beans.
 *
 * <p>Set {@code otel.export.enabled=false} in application config to disable OTLP export
 * (useful in development/test environments).
 */
@Factory
public class OtelConfig {

    private static final Logger log = LoggerFactory.getLogger(OtelConfig.class);

    @Bean
    SdkTracerProvider sdkTracerProvider() {
        boolean enabled = Config.enabled("otel.export.enabled", true);
        if (enabled) {
            log.info("exporting otel traces");
        }
        SpanExporter exporter = enabled ? OtlpGrpcSpanExporter.getDefault() : NoopSpanExporter.getInstance();
        SimpleSpanProcessor spanProcessor = SimpleSpanProcessor.builder(exporter).build();
        return SdkTracerProvider.builder()
                .addSpanProcessor(spanProcessor)
                .build();
    }

    @Bean
    OpenTelemetry openTelemetry(SdkTracerProvider sdkTracerProvider) {
        return OpenTelemetrySdk.builder()
                .setTracerProvider(sdkTracerProvider)
                .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                .build();
    }

    private Instrumenter<ServerRequest, ServerResponse> instrumenter(OpenTelemetry openTelemetry) {
        return DefaultHttpServerInstrumenterBuilder.create(
                        "io.opentelemetry.helidon",
                        openTelemetry,
                        new HelidonAttributesGetter(),
                        new HelidonRequestGetter())
                .build();
    }

    @Bean
    Filter filter(OpenTelemetry openTelemetry) {
        return new OpenTelemetryFilter(instrumenter(openTelemetry));
    }
}
