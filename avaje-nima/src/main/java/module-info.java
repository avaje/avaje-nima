import io.avaje.nima.provider.DefaultConfigProvider;

module io.avaje.nima {

  exports io.avaje.nima;

  requires transitive io.avaje.config;
  requires transitive io.avaje.http.api;
  requires transitive io.avaje.inject;
  requires transitive io.avaje.jsonb;
  requires transitive io.avaje.jsonb.plugin;
  requires transitive io.helidon.nima.webserver;

  provides io.avaje.inject.spi.Plugin with DefaultConfigProvider;
}
