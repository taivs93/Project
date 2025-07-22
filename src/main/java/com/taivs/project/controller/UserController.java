package com.taivs.project.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.taivs.project.dto.request.NameChangeRequest;
import com.taivs.project.dto.response.PagedResponse;
import com.taivs.project.dto.response.ResponseDTO;
import com.taivs.project.dto.response.UserResponseDTO;
import com.taivs.project.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PatchMapping("/change-name")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO> updateUserDetails(@Valid @RequestBody NameChangeRequest req){
        return ResponseEntity.ok(ResponseDTO.builder().status(200).message("User name changed successfully").data(userService.changeUserName(req.getName())).build());
    }

    @GetMapping("/get-user-details/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ResponseDTO> getUserDetailsById(@PathVariable Long id){
        return ResponseEntity.ok(ResponseDTO.builder().status(200).message("Get user details successfully").data(userService.getUserDetailsById(id)).build());
    }

    @PatchMapping("/active-user/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO> activeUser(@PathVariable Long id){
        return ResponseEntity.ok(ResponseDTO.builder().status(200).message("Active user successfully")
                .data(userService.activeUser(id)).build());
    }

    @PatchMapping("/deactive-user/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO> deactiveUser(@PathVariable Long id){
        return ResponseEntity.ok(ResponseDTO.builder().status(200).message("Deactive user successfully")
                .data(userService.deActiveUser(id)).build());
    }

    @GetMapping("/get-users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO> getAllUsers(
            @RequestParam(required = false) String tel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        Page<UserResponseDTO> users = userService.getUsers(tel,pageable);

        PagedResponse<UserResponseDTO> pagedResponse = new PagedResponse<>(
                users.getContent(),
                users.getNumber(),
                users.getSize(),
                users.getTotalElements(),
                users.getTotalPages(),
                users.isLast()
        );

        return ResponseEntity.ok(ResponseDTO.builder()
                .status(200)
                .message("Get all users successfully")
                .data(pagedResponse)
                .build());
    }
}


