package com.taivs.project.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.taivs.project.validation.passwordMatches.PasswordMatches;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import com.taivs.project.validation.password.ValidPassword;
import com.taivs.project.validation.phone.ValidPhone;

@Getter
@PasswordMatches
public class RegisterRequest {

    @NotEmpty(message = "Tel is required")
    @ValidPhone
    @Size(max = 10, message = "Tel must be at most 10 characters")
    private String tel;

    @NotEmpty(message = "Name is required")
    @Size(max = 250, message = "Name must be at most 250 characters")
    private String name;

    @ValidPassword
    @NotEmpty(message = "Password is required")
    @Size(max = 250, message = "Password must be at most 250 characters")
    private String password;

    @NotEmpty(message = "Address is required")
    @Size(max = 400, message = "Address must be at most 400 characters")
    private String address;

    @ValidPassword
    @NotEmpty(message = "Retype password is required")
    @Size(max = 250, message = "Retype password must be at most 250 characters")
    @JsonProperty("retype_password")
    private String retypePassword;
}
