package com.taivs.project.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

@Getter
public class LogoutRequestDTO {

    @NotEmpty(message = "Device id not null")
    @JsonProperty("device_id")
    private String deviceId;
}
