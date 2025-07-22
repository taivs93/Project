package com.taivs.project.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OwnCustomerResponse {

    private Long id;
    private String tel;
    private String name;
    private List<ReportResponseDTO> reports;

    @JsonProperty("total_packages")
    private Long totalPackages;
}
