package com.taivs.project.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class WarehouseDTO {

    @JsonProperty("name")
    @Size(max = 250,message = "Name's length must be in range 1 to 250")
    private String name;

    @JsonProperty("location")
    @Size(max = 250,message = "Name's length must be in range 1 to 250")
    private String location;
}
