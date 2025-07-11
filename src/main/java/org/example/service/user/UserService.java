package org.example.service.user;

import org.example.dto.response.CustomerLiteDTO;
import org.example.dto.response.CustomerResponseDTO;
import org.example.dto.response.UserResponseDTO;

import java.util.List;

public interface UserService {

    UserResponseDTO getUserDetails();

    UserResponseDTO deActiveUser(Long userId);

    UserResponseDTO activeUser(Long userId);

    UserResponseDTO changeUserName(String name);

    CustomerLiteDTO getCustomerDetails(Long id);

    CustomerResponseDTO getCustomerDetails(String tel);

    List<UserResponseDTO> findAllUsers();
}
