package org.example.service.user;

import org.example.dto.response.UserResponseDTO;
import org.example.entity.User;

import java.util.List;

public interface UserService {

    UserResponseDTO getUserDetails();

    UserResponseDTO deActiveUser(Long userId);

    UserResponseDTO activeUser(Long userId);

    UserResponseDTO changeUserName(String name);

    UserResponseDTO getUserDetailsById(Long id);

    List<UserResponseDTO> findAllUsers();
}
