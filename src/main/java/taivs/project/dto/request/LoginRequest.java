package taivs.project.dto.request;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import taivs.project.validation.password.ValidPassword;
import taivs.project.validation.phone.ValidPhone;

@Data
public class LoginRequest {

    @ValidPhone
    @NotEmpty
    private String tel;

    @ValidPassword
    @NotEmpty
    private String password;
}
