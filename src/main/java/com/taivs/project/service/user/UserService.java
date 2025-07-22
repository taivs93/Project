package com.taivs.project.service.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.taivs.project.dto.response.UserResponseDTO;

public interface UserService {

    UserResponseDTO deActiveUser(Long userId);

    UserResponseDTO activeUser(Long userId);

    UserResponseDTO changeUserName(String name);

    UserResponseDTO getUserDetailsById(Long id);

    Page<UserResponseDTO> getUsers(String userTel, Pageable pageable);
}
