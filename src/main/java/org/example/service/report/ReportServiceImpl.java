package org.example.service.report;

import org.example.dto.request.ReportDTO;
import org.example.dto.response.ReportResponseDTO;
import org.example.entity.Customer;
import org.example.entity.Report;
import org.example.entity.User;
import org.example.exception.DataNotFoundException;
import org.example.exception.UnauthorizedAccessException;
import org.example.repository.CustomerRepository;
import org.example.repository.ReportRepository;
import org.example.repository.UserRepository;
import org.example.service.auth.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AuthService authService;

    private ReportResponseDTO toDTO(Report report){

        return ReportResponseDTO.builder().id(report.getId()).customerId(report.getCustomer().getId())
                .description(report.getDescription()).build();
    }

    public ReportResponseDTO insertReport(ReportDTO reportDTO){
        User user = authService.getCurrentUser();
        Customer customer = customerRepository.findById(reportDTO.getCustomerId()).orElseThrow(() -> new DataNotFoundException("Customer not found"));
        if (!customer.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("Not authorized to add this report");
        }
        Report newReport = Report.builder().customer(customer).user(user).description(reportDTO.getDescription()).build();
        List<Report> reports = user.getReports();
        reports.add(newReport);
        List<Customer> customers = customerRepository.findByTel(user.getTel());
        for (Customer c : customers) {
            c.setReports(c.getReports());
        }
        user.setReports(reports);
        reportRepository.save(newReport);
        return toDTO(newReport);
    }

    public void deleteReport(Long id){
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Report not found"));
        User user = authService.getCurrentUser();
        if (!report.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("Not authorized to delete this report");
        }
        reportRepository.delete(report);
    }
}
