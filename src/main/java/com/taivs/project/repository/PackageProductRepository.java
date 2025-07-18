package com.taivs.project.repository;

import com.taivs.project.entity.PackageProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PackageProductRepository extends JpaRepository<PackageProduct, Long> {
}
