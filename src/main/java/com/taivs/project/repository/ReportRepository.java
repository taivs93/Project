package com.taivs.project.repository;

import com.taivs.project.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    @Query("""
            SELECT r FROM Report r
            WHERE r.user.id = :userId AND r.customer.id = :customerId
            """)
    List<Report> getReportsByUserIdAndCustomerId(@Param("userId") Long userId, @Param("customerId") Long customerId);
}
