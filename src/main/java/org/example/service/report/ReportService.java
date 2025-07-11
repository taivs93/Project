package org.example.service.report;

import org.example.dto.request.ReportDTO;
import org.example.dto.response.ReportResponseDTO;

public interface ReportService {

    ReportResponseDTO insertReport(ReportDTO reportDTO);

    void deleteReport(Long id);
}
