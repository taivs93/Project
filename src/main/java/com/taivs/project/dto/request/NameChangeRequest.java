package com.taivs.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class NameChangeRequest {

    @Size(max = 250, message = "Name must be at most 250 characters")
    @NotEmpty(message = "Name is required")
    private String name;
}
