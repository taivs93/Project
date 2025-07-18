package com.taivs.project.dto.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PackageProductResponseDTO {

    private Long id;

    ProductResponseDTO productResponseDTO;

    private int quantity;

}
