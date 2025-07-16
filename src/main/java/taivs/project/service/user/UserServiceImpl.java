package taivs.project.service.user;

import taivs.project.dto.response.UserResponseDTO;
import taivs.project.entity.User;
import taivs.project.exception.DataNotFoundException;
import taivs.project.exception.ResourceAlreadyExistsException;

import taivs.project.repository.CustomerRepository;
import taivs.project.repository.TokenRepository;
import taivs.project.repository.UserRepository;
import taivs.project.service.auth.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
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
