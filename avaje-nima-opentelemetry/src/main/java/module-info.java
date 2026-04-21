module io.avaje.nima.opentelemetry {

  exports io.avaje.nima.opentelemetry;

  requires transitive io.avaje.config;
  requires transitive io.avaje.inject;
  requires transitive io.helidon.webserver;
  requires transitive io.opentelemetry.api;
  requires transitive io.opentelemetry.context;
  requires transitive io.opentelemetry.sdk.trace;
  requires io.opentelemetry.sdk;
  requires io.opentelemetry.sdk.common;
  requires io.opentelemetry.exporter.otlp;
  requires io.opentelemetry.instrumentation_api;
  requires io.opentelemetry.instrumentation_api_incubator;
  requires org.slf4j;
  requires static org.jspecify;
}
