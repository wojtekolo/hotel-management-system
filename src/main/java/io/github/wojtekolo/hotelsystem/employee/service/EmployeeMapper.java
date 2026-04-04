package io.github.wojtekolo.hotelsystem.employee.service;

import io.github.wojtekolo.hotelsystem.employee.model.Employee;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    default String toEmployeeFullName(Employee employee){
        if (employee == null || employee.getPerson() == null) return null;
        return employee.getPerson().getFullName();
    }
}
