package com.taivs.project.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RefreshTokenResponse {

    @JsonProperty("new_access_token")
    String newAccessToken;

    @JsonProperty("new_refresh_token")
    String newRefreshToken;

    @JsonProperty("user")
    UserResponseDTO userResponseDTO;
}
