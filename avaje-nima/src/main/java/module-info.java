module io.avaje.nima {

  exports io.avaje.nima;

  requires transitive io.avaje.config;
  requires transitive io.avaje.http.api;
  requires transitive io.avaje.inject;
  requires transitive io.avaje.jsonb;
  requires transitive io.avaje.jsonb.plugin;
  requires transitive io.helidon.webserver;

  requires static java.net.http;
  requires static io.avaje.spi;
  provides io.avaje.inject.spi.InjectExtension with io.avaje.nima.provider.DefaultConfigProvider;
 }
