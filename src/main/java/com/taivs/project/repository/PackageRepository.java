package com.taivs.project.repository;

import com.taivs.project.entity.Package;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface PackageRepository extends JpaRepository<Package, Long>{

    @Query("SELECT p FROM Package p " +
            "WHERE (:customer_tel IS NULL OR p.customer.tel = :customer_tel) " +
            "AND (:id IS NULL OR p.id = :id)" +
            "AND p.isDraft = 1" +
            "AND p.user.id = :user_id")
    Page<Package> findDraftPackagesByCustomerTelOrId(@Param("user_id") Long userId, @Param("customer_tel") String customerTel, @Param("id") Long id, Pageable pageable);

    @Query(value = """
     SELECT COALESCE(SUM(total_fee), 0)
     FROM packages
     WHERE DATE(created_at) >= CURDATE()
       AND DATE(created_at) < CURDATE() + INTERVAL 1 DAY
       AND created_by = :user_id
       AND status = 20
     """, nativeQuery = true)
    double getTodayRevenue(@Param("user_id") Long userId);

    @Query(value = """
     SELECT COALESCE(SUM(total_fee), 0)
     FROM packages
WHERE DATE(created_at) >= DATE_FORMAT(NOW(), '%Y-%m-01')
  AND DATE(created_at) <  DATE_FORMAT(NOW() + INTERVAL 1 MONTH, '%Y-%m-01')
       AND created_by = :user_id
       AND status = 20
     """, nativeQuery = true)
    double getThisMonthRevenue(@Param("user_id") Long userId);

    @Query(value = """
     SELECT COALESCE(SUM(total_fee), 0)
     FROM packages
     WHERE DATE(created_at) >= DATE_FORMAT(NOW(), '%Y-01-01')
     AND DATE(created_at) <  DATE_FORMAT(NOW() + INTERVAL 1 YEAR, '%Y-01-01')
     AND status = 20
     AND created_by = :user_id
     """, nativeQuery = true)
    double getThisYearRevenue(@Param("user_id") Long userId);

    Optional<Package> findPackageById(Long id);

    @Query("""
            SELECT p FROM Package p
            WHERE (:user_id IS NULL OR p.user.id = :user_id)
            AND (:customer_tel IS NULL OR p.customer.tel LIKE %:customer_tel%)
            AND (:id IS NULL OR p.id = :id)
            """)
    Page<Package> getPackages(@Param("user_id") Long userId, @Param("customer_tel") String customerTel, @Param("id") Long id, Pageable pageable);
}
