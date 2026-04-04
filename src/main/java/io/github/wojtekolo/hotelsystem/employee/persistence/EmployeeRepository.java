package io.github.wojtekolo.hotelsystem.employee.persistence;

import io.github.wojtekolo.hotelsystem.employee.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}
