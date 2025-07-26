package com.taivs.project.repository;

import com.taivs.project.entity.Product;
import com.taivs.project.entity.TopRevenueProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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


    @Query(value = """
    SELECT p.*
    FROM products p
    WHERE p.is_deleted = 0 AND p.created_by = :user_id
    ORDER BY p.stock DESC
    LIMIT 10
    """, nativeQuery = true)
    List<Product> findTop10StockProducts(@Param("user_id") Long userId);

}
