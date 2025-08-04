package com.taivs.project.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Getter;
import com.taivs.project.dto.request.ShipPayer;

import java.util.List;

@Getter
@Builder
public class PackageResponseDTO {

    private Long id;

    @JsonProperty("customer_name")
    private String customerName;

    @JsonProperty("customer_tel")
    private String customerTel;

    private String address;

    private double value;

    @Enumerated(EnumType.STRING)
    @JsonProperty("ship_payer")
    private ShipPayer shipPayer;

    @JsonProperty("ship_money")
    private double shipMoney;

    @JsonProperty("pick_money")
    private double pickMoney;

    @JsonProperty("extra_fee")
    private double extraFee;

    @JsonProperty("total_fee")
    private double totalFee;

    private int status;

    @JsonProperty("is_draft")
    private byte isDraft;

    @JsonProperty("user_id")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long userId;

    private List<PackageProductResponseDTO> packageItems;
}
