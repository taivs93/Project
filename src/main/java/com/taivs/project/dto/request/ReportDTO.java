package com.taivs.project.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

@Getter
public class ReportDTO {

    @NotEmpty(message = "Description can not be empty")
    @Length(max = 100)
    private String description;

    @NotNull(message = "Customer ID is required")
    @JsonProperty("customer_id")
    @Positive
    private Long customerId;

    @NotNull(message = "Package ID is required")
    @JsonProperty("package_id")
    @Positive
    private Long packageId;
}
