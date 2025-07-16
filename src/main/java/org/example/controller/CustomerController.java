package org.example.controller;

import org.example.dto.response.ResponseDTO;
import org.example.service.customer.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/get-customer-details-id/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseDTO> getCustomerDetailsById(@PathVariable Long id){
        return ResponseEntity.ok(ResponseDTO.builder().status(200).message("Get customer details successfully").data(customerService.getCustomerDetails(id)).build());
    }

    @GetMapping("/get-customer-details-tel/{tel}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseDTO> getCustomerDetailsByTel(@PathVariable String tel){
        return ResponseEntity.ok(ResponseDTO.builder().status(200).message("Get customer details successfully").data(customerService.getCustomerDetails(tel)).build());
    }

    @GetMapping("/get-list-customers")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseDTO> getListCustomers(){
        return ResponseEntity.ok(ResponseDTO.builder().status(200).message("Get list customers successfully")
                .data(customerService.getListCustomers()).build());
    }
}
