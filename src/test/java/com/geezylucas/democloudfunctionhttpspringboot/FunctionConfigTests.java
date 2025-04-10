package com.geezylucas.democloudfunctionhttpspringboot;

import com.geezylucas.democloudfunctionhttpspringboot.dto.PubSubBodyDTO;
import com.geezylucas.democloudfunctionhttpspringboot.dto.PubSubMessageDTO;
import com.google.cloud.spring.pubsub.core.publisher.PubSubPublisherTemplate;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.function.context.FunctionCatalog;
import org.springframework.cloud.function.context.test.FunctionalSpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.messaging.Message;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@FunctionalSpringBootTest
class FunctionConfigTests {

    @MockBean
    private PubSubPublisherTemplate pubSubPublisherTemplate;

    @Autowired
    private FunctionCatalog catalog;

    public static MockWebServer mockBackEnd;

    @BeforeAll
    static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start(7071);
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @Test
    void testSample() {
        when(pubSubPublisherTemplate.publish(anyString(), anyString())).thenReturn(CompletableFuture.completedFuture("2070443601311542"));

        mockBackEnd.enqueue(new MockResponse().setBody(successResponseBody()).setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE));

        Function<Mono<PubSubBodyDTO>, Message<String>> function = catalog.lookup(Function.class, "function");
        Message<String> result = function.apply(requestBodyPubSubMessage());

        assertEquals("2070443601311542", result.getPayload());
    }

    private Mono<PubSubBodyDTO> requestBodyPubSubMessage() {
        return Mono.just(PubSubBodyDTO.builder()
                .deliveryAttempt(2)
                .message(PubSubMessageDTO.builder()
                        .data("Mg==")
                        .messageId("2070443601311540")
                        .publishTime("2021-02-26T19:13:55.749Z")
                        .build())
                .build());
    }

    private String successResponseBody() {
        return """
                {
                  "id": 2,
                  "title": "Eyeshadow Palette with Mirror",
                  "description": "The Eyeshadow Palette with Mirror offers a versatile range of eyeshadow shades for creating stunning eye looks. With a built-in mirror, it's convenient for on-the-go makeup application.",
                  "price": 19.99
                }
                """;
    }
}
