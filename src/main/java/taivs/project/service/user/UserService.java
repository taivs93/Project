package taivs.project.service.user;

import taivs.project.dto.response.UserResponseDTO;

import java.util.List;

public interface UserService {

    UserResponseDTO getUserDetails();

    UserResponseDTO deActiveUser(Long userId);

    UserResponseDTO activeUser(Long userId);

    UserResponseDTO changeUserName(String name);

    UserResponseDTO getUserDetailsById(Long id);

    List<UserResponseDTO> findAllUsers();
}
