package org.example.dto.request;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.example.validation.password.ValidPassword;
import org.example.validation.phone.ValidPhone;

@Data
public class LoginRequest {

    @ValidPhone
    @NotEmpty
    private String tel;

    @ValidPassword
    @NotEmpty
    private String password;
}
