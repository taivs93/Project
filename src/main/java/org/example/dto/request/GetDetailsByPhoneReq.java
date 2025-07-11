package org.example.dto.request;

import lombok.Getter;
import org.example.validation.phone.ValidPhone;

@Getter
public class GetDetailsByPhoneReq {

    @ValidPhone
    private String tel;

}
