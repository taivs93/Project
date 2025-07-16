package taivs.project.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CustomerResponseDTO {

    private String tel;

    private String type;

    @JsonProperty(value = "total_packages")
    private Integer totalPackages;

    List<ReportResponseDTO> reports;
}
