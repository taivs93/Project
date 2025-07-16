package org.example.service.customer;


import org.example.dto.response.CustomerLiteDTO;
import org.example.dto.response.CustomerResponseDTO;

import java.util.List;

public interface CustomerService {

    CustomerLiteDTO getCustomerDetails(Long id);

    CustomerResponseDTO getCustomerDetails(String tel);

    List<CustomerLiteDTO> getListCustomers();
}
