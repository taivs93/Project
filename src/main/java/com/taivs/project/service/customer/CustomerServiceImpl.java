package com.taivs.project.service.customer;

import com.taivs.project.dto.request.CustomerDTO;
import com.taivs.project.dto.response.CustomerLiteDTO;
import com.taivs.project.dto.response.CustomerResponseDTO;
import com.taivs.project.dto.response.OwnCustomerResponse;
import com.taivs.project.dto.response.ReportResponseDTO;
import com.taivs.project.entity.Customer;
import com.taivs.project.entity.CustomerType;
import com.taivs.project.entity.Report;
import com.taivs.project.entity.User;
import com.taivs.project.exception.DataNotFoundException;
import com.taivs.project.exception.ResourceAlreadyExistsException;
import com.taivs.project.exception.UnauthorizedAccessException;
import com.taivs.project.repository.CustomerRepository;
import com.taivs.project.repository.PackageRepository;
import com.taivs.project.repository.ReportRepository;
import com.taivs.project.service.auth.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CustomerServiceImpl implements CustomerService{

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private PackageRepository packageRepository;

    @Override
    public OwnCustomerResponse getCustomerDetails(Long id){
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new DataNotFoundException("Customer not found"));

        if(!customer.getUser().equals(authService.getCurrentUser())){
            throw new UnauthorizedAccessException("Unauthorize");
        }

        List<Report> reportsByUserIdAndCustomerId = reportRepository.getReportsByUserIdAndCustomerId(authService.getCurrentUser().getId(), customer.getId());

        List<ReportResponseDTO> reportResponseDTOS = reportsByUserIdAndCustomerId.stream()
                .map(report -> ReportResponseDTO.builder().id(report.getId()).description(report.getDescription()).build())
                .toList();

        long totalPackagesWithUser = Optional.ofNullable(packageRepository.countPackagesByCustomerTelAndUserId(customer.getTel(), customer.getUser().getId())).orElse(0L) ;

        return OwnCustomerResponse.builder().id(customer.getId())
                .tel(customer.getTel())
                .name(customer.getName())
                .reports(reportResponseDTOS)
                .totalPackages(totalPackagesWithUser)
                .build();
    }

    @Override
    public CustomerResponseDTO getCustomerDetailsByTel(String tel){

        List<Customer> customers = customerRepository.findByTel(tel);

        int totalPackages = Optional.ofNullable(packageRepository.countPackagesByCustomerTel(tel)).orElse(0);

        Customer customer = customers.get(0);

        List<ReportResponseDTO> reportResponseDTOS = customer.getReports().stream().map(report -> ReportResponseDTO.builder().id(report.getId()).description(report.getDescription()).build()).toList();

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
    public Page<CustomerLiteDTO> getListCustomers(int page, int size, String sortField, String sortDirection, String customerTel) {
        User user = authService.getCurrentUser();
        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(sortDirection);
        } catch (IllegalArgumentException e) {
            direction = Sort.Direction.ASC;
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(direction, sortField)
        );
        Page<Customer> customers = customerRepository.getListCustomer(pageable, user.getId(),customerTel);
        return customers.map(customer -> CustomerLiteDTO.builder().id(customer.getId())
                .name(customer.getName())
                .tel(customer.getTel())
                .build());
    }

    @Override
    public CustomerLiteDTO updateCustomerInfo(Long id,CustomerDTO customerDTO) {
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new DataNotFoundException("Customer not found"));
        User user = authService.getCurrentUser();
        if (!Objects.equals(customer.getUser().getId(), user.getId())) throw new UnauthorizedAccessException("Access denied to this data");
        Optional<Customer> existsCustomer = customerRepository.getCustomerByTelAndUserId(customerDTO.getTel(), user.getId());

        if (existsCustomer.isPresent()) {
            throw new ResourceAlreadyExistsException("Customer tel already existed with this customer id: " + existsCustomer.get().getId());
        }

        Set<Report> reportWithUserIdAndCustomerId = new HashSet<>(reportRepository.getReportsByUserIdAndCustomerId(user.getId(), customer.getId()));

        List<Customer> listCustomersWithOldTel = customerRepository.findByTel(customer.getTel());
        for (Customer c : listCustomersWithOldTel) {
            List<Report> reportList = c.getReports();
            reportList.removeIf(reportWithUserIdAndCustomerId::contains);
            c.setReports(reportList);
        }

        customer.setName(customerDTO.getName());
        customer.setTel(customerDTO.getTel());

        List<Customer> listCustomersWithNewTel = customerRepository.findByTel(customer.getTel());
        for (Customer c : listCustomersWithNewTel) {
            List<Report> reportList = c.getReports();
            reportList.addAll(reportWithUserIdAndCustomerId);
            c.setReports(reportList);
        }
        return CustomerLiteDTO.builder().id(customer.getId()).tel(customerDTO.getTel()).name(customer.getName()).build();
    }
    @Override
    public void deleteCustomerById(Long id) {
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new DataNotFoundException("Customer not found"));
        User user = authService.getCurrentUser();
        if (!Objects.equals(customer.getUser().getId(), user.getId())) throw new UnauthorizedAccessException("Access denied to this data");
        customer.setDeleteStatus(1);
    }

    @Override
    public CustomerLiteDTO insertCustomer(CustomerDTO customerDTO) {
        Optional<Customer> existedCustomer = customerRepository.getCustomerByTelAndUserId(customerDTO.getTel(), authService.getCurrentUser().getId());
        if (existedCustomer.isPresent()) throw new ResourceAlreadyExistsException("Customer tel already existed");
        Customer newCustomer = customerRepository.save(Customer.builder().tel(customerDTO.getTel()).name(customerDTO.getName()).build());
        return CustomerLiteDTO.builder().id(newCustomer.getId()).tel(customerDTO.getTel()).name(customerDTO.getName()).build();
    }

}
