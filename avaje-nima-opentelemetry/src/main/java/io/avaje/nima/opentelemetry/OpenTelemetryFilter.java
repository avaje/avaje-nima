package io.avaje.nima.opentelemetry;

import io.helidon.webserver.http.*;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.opentelemetry.instrumentation.api.semconv.http.HttpServerRoute;
import io.opentelemetry.instrumentation.api.semconv.http.HttpServerRouteSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LoggingEventBuilder;

import java.util.List;

final class OpenTelemetryFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(OpenTelemetryFilter.class);

    private final Instrumenter<ServerRequest, ServerResponse> instrumenter;
    private final List<String> excludedPaths;

    OpenTelemetryFilter(Instrumenter<ServerRequest, ServerResponse> instrumenter, List<String> excludedPaths) {
        this.instrumenter = instrumenter;
        this.excludedPaths = excludedPaths;
    }

    @Override
    public void filter(FilterChain chain, RoutingRequest req, RoutingResponse res) {
        String reqPath = req.path().rawPath();
        if (isExcluded(reqPath)) {
            chain.proceed();
            return;
        }
        Context parentContext = Context.current();
        if (!instrumenter.shouldStart(parentContext, req)) {
            chain.proceed();
            return;
        }
        String reqMethod = req.prologue().method().text();
        Context context = instrumenter.start(parentContext, req);
        Throwable error = null;
        try (Scope ignored = context.makeCurrent()) {
            if (log.isTraceEnabled()) {
                logHttpRequest(log.atTrace(), reqMethod, reqPath).log();
            }
            chain.proceed();
            if (log.isInfoEnabled()) {
                logHttpResponse(log.atInfo(), res, reqMethod, reqPath).log();
            }
        } catch (Throwable t) {
            logHttpResponse(log.atWarn(), res, reqMethod, reqPath).log("Error processing request", t);
            error = t;
            throw t;
        } finally {
            req.matchingPattern()
                    .ifPresent(route -> HttpServerRoute.update(context, HttpServerRouteSource.SERVER, route));
            instrumenter.end(context, req, res, error);
        }
    }

    private boolean isExcluded(String path) {
        for (String prefix : excludedPaths) {
            if (path.startsWith(prefix)) return true;
        }
        return false;
    }

    private static LoggingEventBuilder logHttpRequest(LoggingEventBuilder logEvent, String reqMethod, String reqPath) {
        return logEvent
                .addKeyValue("http_request_method", reqMethod)
                .addKeyValue("http_request_path", reqPath);
    }

    private static LoggingEventBuilder logHttpResponse(LoggingEventBuilder logEvent, RoutingResponse res, String reqMethod, String reqPath) {
        return logEvent
                .addKeyValue("http_request_method", reqMethod)
                .addKeyValue("http_request_path", reqPath)
                .addKeyValue("http_response_status_code", res.status().code());
    }
}
