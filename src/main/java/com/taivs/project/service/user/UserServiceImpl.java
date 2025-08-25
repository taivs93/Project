package com.taivs.project.service.user;

import com.taivs.project.exception.UnauthorizedAccessException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.taivs.project.dto.response.UserResponseDTO;
import com.taivs.project.entity.User;
import com.taivs.project.exception.DataNotFoundException;
import com.taivs.project.exception.ResourceAlreadyExistsException;
import com.taivs.project.repository.CustomerRepository;
import com.taivs.project.repository.UserRepository;
import com.taivs.project.service.auth.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private SessionRepository sessionRepository;

    public UserResponseDTO deActiveUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new DataNotFoundException("User not found"));
        if (user.getStatus() == 1) user.setStatus((byte) 0);
        sessionRepository.deleteAllByUserId(user.getId());
        userRepository.save(user);

        return UserResponseDTO.fromEntity(user);
    }

    public UserResponseDTO activeUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new DataNotFoundException("User not found"));
        if (user.getStatus() == 0) user.setStatus((byte) 1);
        userRepository.save(user);
        return UserResponseDTO.fromEntity(user);
    }

    public UserResponseDTO changeUserName(String name){
        User user = authService.getCurrentUser();
        if (userRepository.existsByName(name)) {
            throw new ResourceAlreadyExistsException("Name already registered");
        }
        user.setName(name);
        userRepository.save(user);
        return getUserDetailsById(user.getId());
    }

    @Override
    public UserResponseDTO getUserDetailsById(Long id) {
        User user = authService.getCurrentUser();
        boolean isAdmin = user.getUserRoles().stream().anyMatch(userRole -> userRole.getRole().getName().equals("ADMIN"));

        if (!user.getId().equals(id) && !isAdmin) throw new UnauthorizedAccessException("Unauthorize to access this resource");

        return UserResponseDTO.fromEntity(userRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("User not found.")));
    }

    @Override
    public Page<UserResponseDTO> getUsers(String tel, Pageable pageable) {
        return userRepository.getUsers(tel, pageable)
                .map(UserResponseDTO::fromEntity);
    }

}
