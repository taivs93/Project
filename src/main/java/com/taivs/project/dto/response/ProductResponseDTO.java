package com.taivs.project.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
@Setter
public class ProductResponseDTO {
    private Long id;
    private String name;
}
