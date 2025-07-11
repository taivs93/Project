package org.example.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import org.example.validation.password.ValidPassword;

@Getter
public class PasswordChangeRequest {

    @NotEmpty(message = "Old password is required")
    @JsonProperty("old_password")
    @ValidPassword
    private String oldPassword;

    @NotEmpty(message = "New password is required")
    @JsonProperty("new_password")
    @ValidPassword
    private String newPassword;

}
