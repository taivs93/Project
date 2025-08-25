package com.taivs.project.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshRequest {

    @NotBlank(message = "Refresh Token must not be null")
    @JsonProperty(value = "refresh_token")
    private String refreshToken;

    @NotBlank(message = "Device id must not be null")
    @JsonProperty(value = "device_id")
    private String deviceId;

}
