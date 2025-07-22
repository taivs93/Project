package com.taivs.project.service.report;

import com.taivs.project.dto.request.ReportDTO;
import com.taivs.project.dto.response.ReportResponseDTO;
import com.taivs.project.entity.Customer;
import com.taivs.project.entity.Report;
import com.taivs.project.entity.User;
import com.taivs.project.exception.DataNotFoundException;
import com.taivs.project.exception.ResourceAlreadyExistsException;
import com.taivs.project.exception.UnauthorizedAccessException;
import com.taivs.project.repository.CustomerRepository;
import com.taivs.project.repository.ReportRepository;
import com.taivs.project.repository.UserRepository;
import com.taivs.project.service.auth.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
        List<Report> reports = user.getReports();
        reports.remove(report);
        List<Customer> customers = customerRepository.findByTel(user.getTel());
        for (Customer c : customers) {
            List<Report> reportList = c.getReports();
            reportList.remove(report);
            c.setReports(reportList);
        }
        user.setReports(reports);
        reportRepository.delete(report);
    }

    @Override
    public ReportResponseDTO getReportId(Long id){
        Report report = reportRepository.findById(id).orElseThrow(() -> new DataNotFoundException("Report not found"));
        User user = authService.getCurrentUser();

        if(!Objects.equals(report.getUser().getId(), user.getId())) throw new UnauthorizedAccessException("Unauthorize to access this data!");

        return ReportResponseDTO.builder().id(report.getId())
                .description(report.getDescription())
                .customerId(report.getCustomer().getId())
                .build();
    }
}
