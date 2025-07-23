package com.taivs.project.controller;

import com.taivs.project.dto.request.CustomerDTO;
import com.taivs.project.dto.response.CustomerLiteDTO;
import com.taivs.project.dto.response.OwnCustomerResponse;
import com.taivs.project.dto.response.ResponseDTO;
import com.taivs.project.service.customer.CustomerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
        return ResponseEntity.ok(ResponseDTO.builder().status(200).message("Get customer details successfully").data(customerService.getCustomerDetailsByTel(tel)).build());
    }

    @GetMapping("/get-list-customers")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseDTO> getListCustomers(@RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "10") int size,
                                                        @RequestParam(defaultValue = "id") String sortField,
                                                        @RequestParam(defaultValue = "ASC") String sortDirection,
                                                        @RequestParam(required = false) String customerTel){

        return ResponseEntity.ok(ResponseDTO.builder().status(200).message("Get list customers successfully")
                .data(customerService.getListCustomers(page,size,sortField,sortDirection,customerTel)).build());
    }

    @PutMapping("/update-info/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseDTO> updateCustomerInfo(@PathVariable Long id, @RequestBody CustomerDTO customerDTO){
        CustomerLiteDTO customerResponse = customerService.updateCustomerInfo(id, customerDTO);
        return ResponseEntity.status(200).body(ResponseDTO.builder().status(200).message("Update customer info successfully!").data(customerResponse).build());
    }

    @PatchMapping("/delete/{id}")
    @PreAuthorize("hasRole('USER)")
    public ResponseEntity<ResponseDTO> deleteCustomer(@PathVariable Long id){
        customerService.deleteCustomerById(id);
        return ResponseEntity.status(204).body(ResponseDTO.builder().status(204).message("Delete customer successfully!").build());
    }

    @PostMapping("/insert")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseDTO> insertCustomer(@Valid @RequestBody CustomerDTO customerDTO){
        return ResponseEntity.status(201).body(ResponseDTO.builder().status(201).message("Insert customer successfully!")
                .data(customerService.insertCustomer(customerDTO)).build());
    }

}
