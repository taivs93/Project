package com.taivs.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshRequest {

    @NotBlank(message = "Refresh Token must not be null")
    private String refreshToken;

}
