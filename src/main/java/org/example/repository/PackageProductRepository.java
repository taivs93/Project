package org.example.repository;

import org.example.entity.PackageProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PackageProductRepository extends JpaRepository<PackageProduct, Long> {
}
