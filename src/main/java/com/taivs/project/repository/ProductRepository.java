package com.taivs.project.repository;

import com.taivs.project.dto.response.TopRiskStock;
import com.taivs.project.entity.Product;
import com.taivs.project.dto.response.TopRevenueProduct;
import com.taivs.project.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Product p WHERE p.name = :name AND p.user.id = :userId AND p.isDeleted = 0")
    boolean existsByNameAndUserId(@Param("name") String name, @Param("userId") Long userId);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Product p WHERE p.barcode = :barcode AND p.user.id = :userId AND p.isDeleted = 0")
    boolean existsByBarcodeAndUserId(@Param("barcode") String barcode, @Param("userId") Long userId);

    @Query("SELECT p FROM Product p " +
            "WHERE (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:barcode IS NULL OR LOWER(p.barcode) LIKE LOWER(CONCAT('%', :barcode, '%')))" +
            "AND p.isDeleted = 0" +
            "AND p.user.id = :id")
    Page<Product> findByNameContainingAndBarcodeContaining(@Param("id") Long id ,@Param("name") String name,@Param("barcode") String barcode, Pageable pageable);

    @Query(value = "SELECT COUNT(*) FROM packages p " +
            "JOIN package_products pp ON p.id = pp.package_id " +
            "JOIN products prod ON pp.product_id = prod.id " +
            "WHERE prod.id = :productId AND p.status IN (5, 7, 11)", nativeQuery = true)
    int countProductInPackage(@Param("productId") Long productId);

    @Query("SELECT p.weight FROM Product p WHERE p.id = :productId")
    Double getWeightById(@Param("productId") Long productId);

    @Query("SELECT p FROM Product p WHERE p.id = :id AND p.isDeleted = 0")
    Optional<Product> findById(@Param("id") Long id);

    @Query(value = """
    SELECT p.id, p.name, SUM(pp.quantity) AS totalQuantity
    FROM products p
    JOIN package_products pp ON p.id = pp.product_id
    JOIN packages pk ON pp.package_id = pk.id
    WHERE pk.status = 20 AND pk.created_by = :userId AND p.is_deleted = 0
    GROUP BY p.id
    ORDER BY totalQuantity DESC
    LIMIT 10
""", nativeQuery = true)
    List<TopRevenueProduct> findTop10RevenueProducts(@Param("userId") Long userId);

    List<Product> findAllByIdInAndUser(Collection<Long> ids, User user);


    @Query(value = """
    SELECT
        agg.id AS id,
        agg.name AS name,
        agg.stockQty AS stockQty,
        agg.avgSalesPerDay AS avgSalesPerDay,
        agg.stockValue AS stockValue,
        agg.maxStockValue AS maxStockValue,
        ROUND(
            CASE
                WHEN agg.stockQty = 0 THEN 0
                WHEN agg.avgSalesPerDay = 0 AND DATEDIFF(NOW(), agg.createdAt) <= 7 THEN 50
                WHEN agg.avgSalesPerDay = 0 THEN 999.99
                WHEN agg.maxStockValue = 0 THEN agg.stockQty / agg.avgSalesPerDay
                ELSE (agg.stockQty / agg.avgSalesPerDay) * (agg.stockValue / agg.maxStockValue)
            END
        , 2) AS inventoryRiskScore,
        ROUND(
            CASE 
                WHEN agg.avgSalesPerDay = 0 THEN NULL
                ELSE agg.stockQty / agg.avgSalesPerDay 
            END
        , 1) AS daysToSellOut,
        ROUND(
            CASE 
                WHEN agg.maxStockValue = 0 THEN 0
                ELSE (agg.stockValue / agg.maxStockValue) * 100 
            END, 1) AS stockValuePercentage
    FROM (
        SELECT
            p.id,
            p.name,
            SUM(COALESCE(i.quantity, 0)) AS stockQty,
            ROUND(
                COALESCE(SUM(pp.quantity), 0) / 
                GREATEST(
                    COALESCE(DATEDIFF(NOW(), p.created_at), 1),
                    1
                ),
            2) AS avgSalesPerDay,
            (p.price * SUM(COALESCE(i.quantity, 0))) AS stockValue,
            (
                SELECT MAX(p2.price * totalInv.quantitySum)
                FROM products p2
                JOIN (
                    SELECT i2.product_id, SUM(i2.quantity) AS quantitySum
                    FROM inventories i2
                    JOIN warehouses w2 ON w2.id = i2.warehouse_id
                    WHERE i2.is_deleted = 0
                      AND (:warehouseId IS NULL OR w2.id = :warehouseId)
                    GROUP BY i2.product_id
                ) totalInv ON p2.id = totalInv.product_id
                WHERE p2.is_deleted = 0
                  AND p2.created_by = :userId
            ) AS maxStockValue,
            p.created_at AS createdAt
        FROM products p
        LEFT JOIN inventories i
            ON p.id = i.product_id
            AND i.is_deleted = 0
        LEFT JOIN warehouses w
            ON w.id = i.warehouse_id
            AND (:warehouseId IS NULL OR w.id = :warehouseId)
        LEFT JOIN package_products pp
            ON p.id = pp.product_id
        LEFT JOIN warehouses wpp
            ON wpp.id = pp.warehouse_id
            AND (:warehouseId IS NULL OR wpp.id = :warehouseId)
        LEFT JOIN packages pk
            ON pp.package_id = pk.id
               AND pk.status = 20
               AND pk.created_by = :userId
        WHERE p.is_deleted = 0
          AND p.created_by = :userId
        GROUP BY p.id, p.name, p.price, p.created_at
    ) AS agg
    ORDER BY 
        CASE 
            WHEN agg.stockQty = 0 THEN 0
            WHEN agg.avgSalesPerDay = 0 THEN 3
            WHEN inventoryRiskScore >= 60 THEN 2
            ELSE 1
        END DESC,
        inventoryRiskScore DESC
    LIMIT :limit
    """, nativeQuery = true)
    List<TopRiskStock> findTopInventoryRiskProducts(
            @Param("userId") Long userId,
            @Param("warehouseId") Long warehouseId,
            @Param("limit") int limit);

}
