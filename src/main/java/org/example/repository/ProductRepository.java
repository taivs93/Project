package org.example.repository;

import org.example.entity.Product;
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
            "AND p.isDeleted = 0")
    Page<Product> findByNameContainingAndBarcodeContaining(@Param("name") String name,@Param("barcode") String barcode, Pageable pageable);

    @Query(value = "SELECT COUNT(*) FROM packages p " +
            "JOIN package_products pp ON p.id = pp.package_id " +
            "JOIN products prod ON pp.product_id = prod.id " +
            "WHERE prod.id = :productId AND p.status IN (5, 7, 11)", nativeQuery = true)
    int countProductInPackage(@Param("productId") Long productId);

    @Query("SELECT p.weight FROM Product p WHERE p.id = :productId")
    Double getWeightById(@Param("productId") Long productId);

    @Query("SELECT p.price FROM Product p WHERE p.id = :productId")
    Double getPriceById(@Param("productId") Long productId);

    Optional<Product> findByIdAndIsDeleted(Long id,byte isDeleted);

    @Query("SELECT p FROM Product p WHERE p.id = :id AND p.isDeleted = 0")
    Optional<Product> findById(@Param("id") Long id);

    @Query("""
    SELECT pp.product 
    FROM PackageProduct pp
    WHERE pp.aPackage.status = 20
    GROUP BY pp.product
    ORDER BY SUM(pp.quantity) DESC
    """)
    Page<Product> findTopRevenueProducts(Pageable pageable);

    @Query("""
    SELECT p FROM Product p
    WHERE p.isDeleted = 0
    ORDER BY p.stock DESC
    """)
    Page<Product> findTopStockProducts(Pageable pageable);

}
