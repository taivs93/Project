package taivs.project.repository;

import taivs.project.entity.PackageProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PackageProductRepository extends JpaRepository<PackageProduct, Long> {
}
