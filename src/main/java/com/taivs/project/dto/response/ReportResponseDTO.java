package com.taivs.project.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReportResponseDTO {

    private Long id;

    private String description;

    @JsonProperty("customer_id")
    private Long customerId;

}
