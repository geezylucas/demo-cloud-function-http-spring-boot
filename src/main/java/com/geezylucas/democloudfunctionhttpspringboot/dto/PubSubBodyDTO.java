package com.geezylucas.democloudfunctionhttpspringboot.dto;

public record PubSubBodyDTO(Integer deliveryAttempt,
                            PubSubMessageDTO message) {
}
