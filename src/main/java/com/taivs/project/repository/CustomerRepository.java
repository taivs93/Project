package com.taivs.project.repository;

import com.taivs.project.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query("""
            SELECT c FROM Customer c
            WHERE c.id = :id AND c.deleteStatus = 0
            """)
    Optional<Customer> findById(@Param("id") Long id);

    @Query("SELECT c FROM Customer c WHERE c.tel = :tel AND c.deleteStatus = 0")
    List<Customer> findByTel(@Param("tel") String tel);

    @Query(value = """
    SELECT * 
    FROM customers c
    WHERE c.created_by = :userId
      AND c.status = 0
      AND (:customerTel IS NULL OR c.tel LIKE CONCAT('%', :customerTel, '%'))
""",
            countQuery = """
    SELECT COUNT(*) 
    FROM customers c
    WHERE c.created_by = :userId
      AND c.status = 0
      AND (:customerTel IS NULL OR c.tel LIKE CONCAT('%', :customerTel, '%'))
""",
            nativeQuery = true)
    Page<Customer> getListCustomer(Pageable pageable,
                                   @Param("userId") Long id,
                                   @Param("customerTel") String customerTel);


    @Query("""
            SELECT c FROM Customer c
            WHERE c.tel = :tel AND c.user.id = :userId AND c.deleteStatus = 0
            """)
    Optional<Customer> getCustomerByTelAndUserId(@Param("tel") String tel, @Param("userId") Long userId);
}
