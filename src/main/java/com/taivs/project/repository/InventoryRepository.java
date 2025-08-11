package com.taivs.project.repository;

import com.taivs.project.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory,Long> {

    @Query("""
    SELECT COALESCE(SUM(i.quantity), 0)
    FROM Inventory i
    WHERE i.product.id = :productId
""")
    Optional<Integer> sumQuantityByProductId(@Param("productId") Long productId);

    @Query(value = """
    SELECT *
    FROM inventories
    WHERE warehouse_id = :warehouseId
          AND product_id = :productId
          AND is_deleted = 0
    """, nativeQuery = true)
    Optional<Inventory> findByWarehouseIdAndProductId(
            @Param("warehouseId") Long warehouseId,
            @Param("productId") Long productId
    );

    @Query(value = """
            SELECT *
            FROM inventories
            WHERE user_id = :userId AND is_deleted = 0
            """,nativeQuery = true)
    Optional<Inventory> findById(@Param("userId") Long userId);
}
