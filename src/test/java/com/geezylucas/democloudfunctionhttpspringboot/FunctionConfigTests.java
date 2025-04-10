package com.geezylucas.democloudfunctionhttpspringboot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.client.TestRestTemplate;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class FunctionConfigTests {

    private final TestRestTemplate rest = new TestRestTemplate();

    @Test
    void testSample() throws IOException, InterruptedException {
        try (LocalServerTestSupport.ServerProcess ignored = LocalServerTestSupport.startServer(DemoCloudFunctionHttpSpringBootApplication.class)) {
            String result = rest.postForObject("http://localhost:8080/", "Hello", String.class);
            assertThat(result).isEqualTo("\"Hello World!\"");
        }
    }
}
