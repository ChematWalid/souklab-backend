package com.project.souklab.integration.chargily;

import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

final class CapturingExchangeFunction implements ExchangeFunction {
    private final AtomicReference<ClientRequest> request = new AtomicReference<>();
    private final ClientResponse response;

    CapturingExchangeFunction(ClientResponse response) {
        this.response = response;
    }

    @Override
    public Mono<ClientResponse> exchange(ClientRequest request) {
        this.request.set(request);
        return Mono.just(response);
    }

    ClientRequest request() {
        return request.get();
    }
}
