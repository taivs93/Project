package org.example.service.customer;

import org.example.dto.response.CustomerLiteDTO;
import org.example.dto.response.CustomerResponseDTO;
import org.example.dto.response.ReportResponseDTO;
import org.example.entity.Customer;
import org.example.entity.CustomerType;
import org.example.entity.User;
import org.example.exception.DataNotFoundException;
import org.example.exception.UnauthorizedAccessException;
import org.example.repository.CustomerRepository;
import org.example.repository.UserRepository;
import org.example.service.auth.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
        return CustomerResponseDTO.builder().id(customer.getId()).tel(customer.getTel()).type(type.getDescription()).totalPackages(totalPackages).reports(reportResponseDTOS).build();
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
