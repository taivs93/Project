package com.taivs.project.repository;

import com.taivs.project.entity.InventoryTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction,Long> {

    @Query("""
            SELECT i
            FROM InventoryTransaction i WHERE i.id = :id AND i.isDeleted = 0
            """)
    Optional<InventoryTransaction> findById(@Param("id") Long id);

    @Query("""
            SELECT i
            FROM InventoryTransaction i WHERE i.user.id = :userId
             AND (i.warehouse.name = :warehouseName OR i.warehouse.name IS NULL)
              AND (i.product.name = :productName OR i.product.name IS NULL)
               AND i.isDeleted = 0
            """)
    Page<InventoryTransaction> getListInventoryTransactions(Pageable pageable,@Param("userId") Long userId,@Param("warehouseName") String warehouseName,@Param("productName") String productName);

}
