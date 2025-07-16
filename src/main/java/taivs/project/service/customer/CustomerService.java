package taivs.project.service.customer;


import taivs.project.dto.response.CustomerLiteDTO;
import taivs.project.dto.response.CustomerResponseDTO;

import java.util.List;

public interface CustomerService {

    CustomerLiteDTO getCustomerDetails(Long id);

    CustomerResponseDTO getCustomerDetails(String tel);

    List<CustomerLiteDTO> getListCustomers();
}
