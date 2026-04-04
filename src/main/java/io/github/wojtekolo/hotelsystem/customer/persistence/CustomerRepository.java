package io.github.wojtekolo.hotelsystem.customer.persistence;

import io.github.wojtekolo.hotelsystem.customer.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
