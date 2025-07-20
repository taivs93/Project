package com.taivs.project.service.customer;


import com.taivs.project.dto.request.CustomerDTO;
import com.taivs.project.dto.response.CustomerLiteDTO;
import com.taivs.project.dto.response.CustomerResponseDTO;

import java.util.List;

public interface CustomerService {

    CustomerLiteDTO getCustomerDetails(Long id);

    CustomerResponseDTO getCustomerDetails(String tel);

    List<CustomerLiteDTO> getListCustomers();

    CustomerLiteDTO updateCustomerInfo(Long id, CustomerDTO customerDTO);

    void deleteCustomerById(Long id);
}
