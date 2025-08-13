package com.taivs.project.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PackageProductResponseDTO {

    @JsonProperty("product")
    ProductResponseDTO productResponseDTO;

    @JsonProperty("quantity")
    private int quantity;

}
