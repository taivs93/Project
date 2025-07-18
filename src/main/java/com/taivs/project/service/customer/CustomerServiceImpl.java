package com.taivs.project.service.customer;

import com.taivs.project.dto.response.CustomerLiteDTO;
import com.taivs.project.dto.response.CustomerResponseDTO;
import com.taivs.project.dto.response.ReportResponseDTO;
import com.taivs.project.entity.Customer;
import com.taivs.project.entity.CustomerType;
import com.taivs.project.entity.User;
import com.taivs.project.exception.DataNotFoundException;
import com.taivs.project.exception.UnauthorizedAccessException;
import com.taivs.project.repository.CustomerRepository;
import com.taivs.project.repository.UserRepository;
import com.taivs.project.service.auth.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService{

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;


    @Override
    public CustomerLiteDTO getCustomerDetails(Long id){
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new DataNotFoundException("Customer not found"));

        if(!customer.getUser().equals(authService.getCurrentUser())){
            throw new UnauthorizedAccessException("Unauthorize");
        }

        return CustomerLiteDTO.builder().tel(customer.getTel()).name(customer.getName()).id(customer.getId()).build();
    }

    @Override
    public CustomerResponseDTO getCustomerDetails(String tel){
        User user = authService.getCurrentUser();
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
    public List<CustomerLiteDTO> getListCustomers() {
        User user = authService.getCurrentUser();
        List<Customer> customers = customerRepository.getListCustomer(user.getId());
        return customers.stream().map(customer ->
                CustomerLiteDTO.builder().id(customer.getId()).name(customer.getName()).tel(customer.getTel()).build())
                .toList();
    }
}
