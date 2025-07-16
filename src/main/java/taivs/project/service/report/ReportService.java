package taivs.project.service.report;

import taivs.project.dto.request.ReportDTO;
import taivs.project.dto.response.ReportResponseDTO;

public interface ReportService {

    ReportResponseDTO insertReport(ReportDTO reportDTO);

    void deleteReport(Long id);
}
