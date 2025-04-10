package com.geezylucas.democloudfunctionhttpspringboot.dto;

import lombok.Builder;

@Builder
public record PubSubBodyDTO(Integer deliveryAttempt,
                            PubSubMessageDTO message) {
}
