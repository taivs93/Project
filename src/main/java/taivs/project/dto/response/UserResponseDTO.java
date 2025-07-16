package taivs.project.dto.response;

import lombok.Builder;

import lombok.Getter;
import lombok.ToString;
import taivs.project.entity.User;

@Getter
@Builder
@ToString
public class UserResponseDTO {

    private Long id;

    private String tel;

    private String name;

    private String address;

    private byte status;

    public static UserResponseDTO fromEntity(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .tel(user.getTel())
                .name(user.getName())
                .address(user.getAddress())
                .status(user.getStatus())
                .build();
    }
}
