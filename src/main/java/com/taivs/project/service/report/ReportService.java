package com.taivs.project.service.report;

import com.taivs.project.dto.request.ReportDTO;
import com.taivs.project.dto.response.ReportResponseDTO;

public interface ReportService {

    ReportResponseDTO insertReport(ReportDTO reportDTO);

    void deleteReport(Long id);

    ReportResponseDTO getReportId(Long id);
} 
