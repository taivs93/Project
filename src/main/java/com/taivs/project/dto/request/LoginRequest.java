package com.taivs.project.dto.request;
import jakarta.validation.constraints.NotEmpty;
import com.taivs.project.validation.password.ValidPassword;
import com.taivs.project.validation.phone.ValidPhone;
import lombok.Getter;

@Getter
public class LoginRequest {

    @ValidPhone
    @NotEmpty(message = "Tel must not be empty")
    private String tel;

    @ValidPassword
    @NotEmpty(message = "Password must not be empty")
    private String password;
}
