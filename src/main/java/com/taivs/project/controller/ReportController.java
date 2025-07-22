package com.taivs.project.controller;

import jakarta.validation.Valid;
import com.taivs.project.dto.request.ReportDTO;
import com.taivs.project.dto.response.ReportResponseDTO;
import com.taivs.project.dto.response.ResponseDTO;
import com.taivs.project.service.report.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Autowired
    ReportService reportService;

    @PostMapping("/insert")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseDTO> insertReport(@Valid @RequestBody ReportDTO reportDTO, BindingResult result){
        ReportResponseDTO reportResponseDTO = reportService.insertReport(reportDTO);
        return ResponseEntity.ok(ResponseDTO.builder().status(201).message("Insert report successfully").data(reportResponseDTO).build());
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseDTO> deleteReport(@PathVariable Long id){
        reportService.deleteReport(id);
        return ResponseEntity.ok(ResponseDTO.builder().status(200).message("Report deleted successfully").build());
    }

    @GetMapping("/get/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseDTO> getReportById(@PathVariable Long id){
        return ResponseEntity.ok(ResponseDTO.builder().status(200).message("Get report by ID successfully").data(reportService.getReportId(id)).build());
    }
}
