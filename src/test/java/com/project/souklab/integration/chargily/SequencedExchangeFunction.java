package com.project.souklab.integration.chargily;

import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

final class SequencedExchangeFunction implements ExchangeFunction {
    private final List<ClientResponse> responses;
    private final AtomicInteger calls = new AtomicInteger();

    SequencedExchangeFunction(List<ClientResponse> responses) {
        this.responses = responses;
    }

    @Override
    public Mono<ClientResponse> exchange(ClientRequest request) {
        int index = calls.getAndIncrement();
        return Mono.just(responses.get(Math.min(index, responses.size() - 1)));
    }

    int calls() {
        return calls.get();
    }
}
