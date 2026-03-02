package io.github.wojtekolo.hotelsystem.employee;

import io.github.wojtekolo.hotelsystem.common.person.Person;
import io.github.wojtekolo.hotelsystem.common.person.PersonTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;

public class EmployeeTestUtils {
    public static Employee.EmployeeBuilder aValidEmployee(Person person){
        return Employee.builder()
                .person(person)
                .description("test description")
                .employeeRole(EmployeeRole.CASHIER)
                .pesel("12345678912")
                .idCardNumber("12345")
                .salary(BigDecimal.valueOf(5000))
                .workEmail("testEmployee@email.com")
                .workPhone("111222333")
                .emergencyPhone("222111333")
                .hireDate(LocalDate.of(2015,10,10));
    }
    public static Employee.EmployeeBuilder aValidEmployee(){
        return aValidEmployee(PersonTestUtils.aValidPerson().build());
    }
}
