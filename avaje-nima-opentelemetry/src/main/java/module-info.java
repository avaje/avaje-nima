module io.avaje.nima.opentelemetry {

  exports io.avaje.nima.opentelemetry;

  requires transitive io.helidon.webserver;
  requires transitive io.opentelemetry.api;
  requires transitive io.opentelemetry.context;
  requires transitive io.opentelemetry.instrumentation_api;
  requires org.slf4j;
  requires static org.jspecify;
}
