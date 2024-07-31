package org.programmingtechie.config;


import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;

import reactor.core.publisher.Mono;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.Context;

public class TraceExchangeFilterFunction implements ExchangeFilterFunction {

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        Context context = Context.current();
        Span span = Span.current();
        if (span.getSpanContext().isValid()) {
            ClientRequest.Builder requestBuilder = ClientRequest.from(request);
            context = context.with(span);
            try (Scope ignored = context.makeCurrent()) {
                return next.exchange(requestBuilder.build());
            }
        } else {
            return next.exchange(request);
        }
    }
}
