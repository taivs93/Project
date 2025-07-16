package org.example.controller;

import jakarta.validation.Valid;
import org.example.dto.request.NameChangeRequest;
import org.example.dto.response.ResponseDTO;
import org.example.service.customer.CustomerService;
import org.example.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/get-user-details")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseDTO> getUserDetails(){
        return ResponseEntity.ok(ResponseDTO.builder().status(200).message("Get user details successfully").data(userService.getUserDetails()).build());
    }

    @PatchMapping("/change-name")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseDTO> updateUserDetails(@Valid @RequestBody NameChangeRequest req){
        return ResponseEntity.ok(ResponseDTO.builder().status(200).message("User name changed successfully").data(userService.changeUserName(req.getName())).build());
    }

    @GetMapping("/get-user-details-by-id/{id}")
    @PreAuthorize("hasRoles('ADMIN')")
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

    @GetMapping("/get-all-users")
    @PreAuthorize("hasRole('ADMIN)")
    public ResponseEntity<ResponseDTO> getAllUsers(){
        return ResponseEntity.ok(ResponseDTO.builder().status(200).message("Get all users successfully")
                .data(userService.findAllUsers()).build());
    }
}


