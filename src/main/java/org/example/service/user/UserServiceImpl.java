package org.example.service.user;


import org.example.dto.response.CustomerLiteDTO;
import org.example.dto.response.CustomerResponseDTO;
import org.example.dto.response.ReportResponseDTO;
import org.example.dto.response.UserResponseDTO;
import org.example.entity.Customer;
import org.example.entity.CustomerType;
import org.example.entity.User;
import org.example.exception.DataNotFoundException;
import org.example.exception.ResourceAlreadyExistsException;
import org.example.exception.UnauthorizedAccessException;
import org.example.repository.CustomerRepository;
import org.example.repository.UserRepository;
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

    private User getCurrentUser() {
        String tel = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByTel(tel)
                .orElseThrow(() -> new UsernameNotFoundException("Tel not found: " + tel));
    }

    public UserResponseDTO getUserDetails(){
        User user = getCurrentUser();
        return UserResponseDTO.fromEntity(user);
    }

    public UserResponseDTO deActiveUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new DataNotFoundException("User not found"));
        if (user.getStatus() == 1) user.setStatus((byte) 0);
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
        User user = getCurrentUser();
        if (userRepository.existsByName(name)) {
            throw new ResourceAlreadyExistsException("Name already registered");
        }
        user.setName(name);
        userRepository.save(user);
        return getUserDetails();
    }

    public CustomerLiteDTO getCustomerDetails(Long id){
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new DataNotFoundException("Customer not found"));

        if(!customer.getUser().equals(getCurrentUser())){
            throw new UnauthorizedAccessException("Unauthorize");
        }

        return CustomerLiteDTO.builder().tel(customer.getTel()).name(customer.getName()).id(customer.getId()).build();
    }

    public CustomerResponseDTO getCustomerDetails(String tel){
        User user = getCurrentUser();
        List<Customer> customers = customerRepository.findByTel(tel);
        int totalPackages = 0;
        for (Customer customer : customers){
            totalPackages += customer.getPackages().size();
        }
        Customer customer = customers.get(0);

        List<ReportResponseDTO> reportResponseDTOS = customer.getReports().stream().map(report -> ReportResponseDTO.builder().id(report.getId()).description(report.getDescription()).customerId(report.getCustomer().getId()).build()).toList();

        CustomerType type ;
        if (reportResponseDTOS.isEmpty()) {
            type = CustomerType.UY_TIN;
        } else if (reportResponseDTOS.size() < 3) {
            type = CustomerType.IT_BOM_HANG;
        } else if (reportResponseDTOS.size() <= 5){
            type = CustomerType.THUONG_XUYEN_BOM_HANG;
        } else {
            type = CustomerType.RAT_HAY_BOM_HANG;
        }

        return CustomerResponseDTO.builder().tel(customer.getTel()).type(type.getDescription()).totalPackages(totalPackages).reports(reportResponseDTOS).build();
    }

    @Override
    public List<UserResponseDTO> findAllUsers() {
        return userRepository.findAllUsers().stream().map(UserResponseDTO::fromEntity).toList();
    }
}
