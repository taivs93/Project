package com.taivs.project.dto.request;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import com.taivs.project.validation.password.ValidPassword;
import com.taivs.project.validation.phone.ValidPhone;

@Data
public class LoginRequest {

    @ValidPhone
    @NotEmpty
    private String tel;

    @ValidPassword
    @NotEmpty
    private String password;
}
