package com.taivs.project.dto.request;

import com.taivs.project.validation.phone.ValidPhone;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CustomerDTO {

    @NotEmpty(message = "Name is required")
    @Size(max = 250, message = "Name must be at most 250 characters")
    private String name;

    @NotEmpty(message = "Tel is required")
    @ValidPhone
    private String tel;

}
