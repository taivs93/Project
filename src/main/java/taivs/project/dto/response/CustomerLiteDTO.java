package taivs.project.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CustomerLiteDTO {
    private Long id;
    private String name;
    private String tel;
}
