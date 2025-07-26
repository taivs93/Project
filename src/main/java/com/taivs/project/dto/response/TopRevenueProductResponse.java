package com.taivs.project.dto.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TopRevenueProductResponse {
    private Long id;
    private String name;
    private Long revenueQuantity;
}
