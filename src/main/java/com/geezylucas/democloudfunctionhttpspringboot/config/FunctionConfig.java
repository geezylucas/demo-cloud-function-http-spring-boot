package com.geezylucas.democloudfunctionhttpspringboot.config;

import com.geezylucas.democloudfunctionhttpspringboot.dto.ProductResponseDTO;
import com.geezylucas.democloudfunctionhttpspringboot.dto.PubSubBodyDTO;
import com.google.cloud.spring.pubsub.core.publisher.PubSubPublisherTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
public class FunctionConfig {

    private final WebClient webClient;
    private final PubSubPublisherTemplate pubSubPublisherTemplate;

    public FunctionConfig(@Value("${external.uri}") String uri,
                          WebClient.Builder webClientBuilder,
                          PubSubPublisherTemplate pubSubPublisherTemplate) {
        this.webClient = webClientBuilder
                .baseUrl(uri)
                .build();
        this.pubSubPublisherTemplate = pubSubPublisherTemplate;
    }

    @Bean
    public Function<Mono<PubSubBodyDTO>, Message<String>> function() {
        return input -> input
                .doOnNext(pubSubBodyDTO -> log.info("PubSub Body: {}", pubSubBodyDTO))
                .map(pubSubBodyDTO -> new String(Base64.getDecoder().decode(pubSubBodyDTO.message().data()), StandardCharsets.UTF_8))
                .doOnNext(pubSubMessage -> log.info("PubSub Message: {}", pubSubMessage))
                .flatMap(s -> webClient
                        .get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/products/{id}")
                                .build(s))
                        .retrieve()
                        .bodyToMono(ProductResponseDTO.class)
                        .log()
                )
                .flatMap(productResponseDTO -> Mono.fromFuture(pubSubPublisherTemplate.publish("my-function-topic", productResponseDTO.title())))
                .map(messageId -> MessageBuilder
                        .withPayload(messageId)
                        .setHeader(FunctionInvoker.HTTP_STATUS_CODE, 200)
                        .build()
                )
                .doOnSuccess(signal -> log.info("Function invoked successfully: {}", signal))
                .block();
    }
}