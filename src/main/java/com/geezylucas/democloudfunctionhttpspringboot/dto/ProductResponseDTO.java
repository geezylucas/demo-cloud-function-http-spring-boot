package com.geezylucas.democloudfunctionhttpspringboot.dto;

import java.math.BigDecimal;

public record ProductResponseDTO(int id,
                                 String title,
                                 String description,
                                 BigDecimal price) {
}
