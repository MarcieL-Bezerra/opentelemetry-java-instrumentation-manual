package com.movieapi;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Component
public class TracingFilter extends OncePerRequestFilter {
    String SERVICE_NAME = System.getenv("OTEL_SERVICE_NAME");

    private static final TextMapGetter<HttpServletRequest> getter =
            new TextMapGetter<HttpServletRequest>() {
                @Override
                public Iterable<String> keys(HttpServletRequest httpServletRequest) {
                    return Collections.list(httpServletRequest.getHeaderNames());
                }

                @Override
                public String get(HttpServletRequest httpServletRequest, String s) {
                    return httpServletRequest.getHeader(s);
                }
            };


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Tracer tracer = GlobalOpenTelemetry.getTracer(SERVICE_NAME);

        Context extractedContext = GlobalOpenTelemetry.getPropagators()
                .getTextMapPropagator()
                .extract(Context.current(), request, getter);

        Span span = tracer.spanBuilder(request.getRequestURI())
                .setSpanKind(SpanKind.SERVER)
                .setParent(extractedContext)
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR);
            throw e;
        } finally {
            span.end();
        }
    }
}
