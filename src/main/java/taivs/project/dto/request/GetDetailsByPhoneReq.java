package taivs.project.dto.request;

import lombok.Getter;
import taivs.project.validation.phone.ValidPhone;

@Getter
public class GetDetailsByPhoneReq {

    @ValidPhone
    private String tel;

}
