package org.example.service.user;

import org.example.dto.response.UserResponseDTO;
import org.example.entity.User;
import org.example.exception.DataNotFoundException;
import org.example.exception.ResourceAlreadyExistsException;

import org.example.repository.CustomerRepository;
import org.example.repository.TokenRepository;
import org.example.repository.UserRepository;
import org.example.service.auth.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private AuthService authService;

    public UserResponseDTO getUserDetails(){
        User user = authService.getCurrentUser();
        return UserResponseDTO.fromEntity(user);
    }

    public UserResponseDTO deActiveUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new DataNotFoundException("User not found"));
        if (user.getStatus() == 1) user.setStatus((byte) 0);
        userRepository.save(user);
        tokenRepository.revokeAllByUser(user);
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
        return getUserDetails();
    }

    @Override
    public UserResponseDTO getUserDetailsById(Long id) {
        return UserResponseDTO.fromEntity(userRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("User not found.")));
    }

    @Override
    public List<UserResponseDTO> findAllUsers() {
        return userRepository.findAllUsers().stream().map(UserResponseDTO::fromEntity).toList();
    }
}
