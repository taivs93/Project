package com.taivs.project.repository;

import com.taivs.project.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WarehouseRepository extends JpaRepository<Warehouse,Long> {

    @Query("""
            SELECT CASE 
                    WHEN COUNT(w) > 0 THEN true
                    ELSE false
                   END
            FROM Warehouse w
            WHERE w.user.id = :userId AND (w.name = :name OR w.location = :location) 
            """)
    boolean existsByUserIdAndNameOrUserIdAndLocation(@Param("userId") Long userId,@Param("name") String name,@Param("location") String location);


    @Query("""
            SELECT w
            FROM Warehouse w
            WHERE w.user = :userId AND w.isDeleted = 0
            """)
    List<Warehouse> getWarehouses(@Param("userId") Long userId);

    @Override
    @Query("""
            SELECT w
            FROM Warehouse w WHERE w.id = :warehouseId AND w.isDeleted = 0
            """)
    Optional<Warehouse> findById(@Param("warehouseId") Long warehouseId);

    @Query(value = """
    SELECT *
    FROM warehouses 
    WHERE id IN :ids 
      AND is_deleted = 0 
      AND created_by = :userId
""",nativeQuery = true)
    List<Warehouse> findAllByIdIn(
            @Param("ids") List<Long> ids,
            @Param("userId") Long userId
    );

    @Query("""
            SELECT w
            FROM Warehouse w 
            WHERE w.user.id = :userId AND w.isMain = 0
            """)
    Optional<Warehouse> findMainWarehouseByUserId(@Param("userId") Long userId);
}
