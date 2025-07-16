package taivs.project.repository;

import taivs.project.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query("SELECT c FROM Customer c WHERE c.tel = :tel")
    List<Customer> findByTel(@Param("tel") String tel);

    @Query("SELECT c FROM Customer c WHERE c.user.id = :userId")
    List<Customer> getListCustomer(@Param("userId") Long id);
}
