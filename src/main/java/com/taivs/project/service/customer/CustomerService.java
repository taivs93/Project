package com.taivs.project.service.customer;


import com.taivs.project.dto.request.CustomerDTO;
import com.taivs.project.dto.response.CustomerLiteDTO;
import com.taivs.project.dto.response.CustomerResponseDTO;
import com.taivs.project.dto.response.OwnCustomerResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CustomerService {

    OwnCustomerResponse getCustomerDetails(Long id);

    CustomerResponseDTO getCustomerDetailsByTel(String tel);

    Page<CustomerLiteDTO> getListCustomers(int size, int page, String sortField, String sortDirection);

    CustomerLiteDTO updateCustomerInfo(Long id, CustomerDTO customerDTO);

    void deleteCustomerById(Long id);

    CustomerLiteDTO insertCustomer(CustomerDTO customerDTO);
}
