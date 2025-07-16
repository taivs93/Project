package taivs.project.service.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import taivs.project.dto.response.UserResponseDTO;

import java.util.List;

public interface UserService {

    UserResponseDTO getUserDetails();

    UserResponseDTO deActiveUser(Long userId);

    UserResponseDTO activeUser(Long userId);

    UserResponseDTO changeUserName(String name);

    UserResponseDTO getUserDetailsById(Long id);

    Page<UserResponseDTO> findAllUsers(Pageable pageable);
}
