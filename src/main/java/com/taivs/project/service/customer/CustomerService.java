package com.taivs.project.service.customer;


import com.taivs.project.dto.response.CustomerLiteDTO;
import com.taivs.project.dto.response.CustomerResponseDTO;

import java.util.List;

public interface CustomerService {

    CustomerLiteDTO getCustomerDetails(Long id);

    CustomerResponseDTO getCustomerDetails(String tel);

    List<CustomerLiteDTO> getListCustomers();
}
