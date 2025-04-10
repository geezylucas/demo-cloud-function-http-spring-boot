package com.geezylucas.democloudfunctionhttpspringboot.config;

import com.geezylucas.democloudfunctionhttpspringboot.dto.ProductResponseDTO;
import com.geezylucas.democloudfunctionhttpspringboot.dto.PubSubBodyDTO;
import com.google.cloud.spring.pubsub.core.publisher.PubSubPublisherTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.function.adapter.gcp.FunctionInvoker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.function.Function;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class FunctionConfig {

    private final WebClient webClient;
    private final PubSubPublisherTemplate pubSubPublisherTemplate;

    @Bean
    public Function<Mono<PubSubBodyDTO>, Message<String>> function() {
        return input -> input
                .doOnNext(it -> log.info("Function invoked with message: {}", it))
                .map(pubSubBodyDTO -> new String(Base64.getDecoder().decode(pubSubBodyDTO.message().data()), StandardCharsets.UTF_8))
                .doOnNext(s -> log.info("Received Pub/Sub message: {}", s))
                .flatMap(s -> webClient
                        .get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/products/{id}")
                                .build(s))
                        .retrieve()
                        .bodyToMono(ProductResponseDTO.class)
                )
                .flatMap(productResponseDTO -> Mono.fromFuture(pubSubPublisherTemplate.publish("my-function-topic", productResponseDTO.title())))
                .map(uuid -> MessageBuilder
                        .withPayload(uuid)
                        .setHeader(FunctionInvoker.HTTP_STATUS_CODE, 200)
                        .build()
                )
                .block();
    }
}