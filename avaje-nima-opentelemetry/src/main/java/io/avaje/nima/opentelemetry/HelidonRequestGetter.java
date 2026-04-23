package io.avaje.nima.opentelemetry;

import io.helidon.http.Header;
import io.helidon.http.HeaderNames;
import io.helidon.webserver.http.ServerRequest;
import io.opentelemetry.context.propagation.TextMapGetter;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;

import static java.util.Collections.emptyIterator;
import static java.util.stream.Collectors.toList;

final class HelidonRequestGetter implements TextMapGetter<ServerRequest> {

    @Override
    public Iterable<String> keys(ServerRequest req) {
        // Materialize the stream to avoid Helidon's HeaderIterator bug where customHeadersIterator
        // can be null when iterating through the lazy iterator
        return req.headers().stream().map(Header::name).collect(toList());
    }

    @Override
    public String get(@Nullable ServerRequest carrier, String key) {
        if (carrier == null) {
            return null;
        }
        return carrier.headers().first(HeaderNames.create(key)).orElse(null);
    }

    @Override
    public Iterator<String> getAll(@Nullable ServerRequest carrier, String key) {
        if (carrier == null) {
            return emptyIterator();
        }
        return carrier.headers().values(HeaderNames.create(key)).iterator();
    }
}
