package com.taivs.project.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import com.taivs.project.validation.phone.ValidPhone;

import java.util.List;

@Getter
public class PackageDTO {

    @Valid
    @NotEmpty(message = "Package items are required")
    @JsonProperty("package_items")
    private List<PackageProductDTO> packageItems;

    @NotEmpty(message = "Address is required")
    @Size(max = 255, message = "Address must be at most 255 characters")
    private String address;

    @NotNull(message = "Ship payer is required")
    @JsonProperty("ship_payer")
    private ShipPayer shipPayer;

    @NotNull(message = "Pick money is required")
    @DecimalMin(value = "0.0", message = "Pick money must be at least 0")
    @JsonProperty("pick_money")
    private Double pickMoney;

    @NotEmpty(message = "Customer name is required")
    @Size(max = 250, message = "Customer name must be at most 250 characters")
    @JsonProperty("customer_name")
    private String customerName;

    @NotEmpty(message = "Customer tel is required")
    @ValidPhone
    @JsonProperty("customer_tel")
    private String customerTel;

}
