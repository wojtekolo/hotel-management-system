package io.github.wojtekolo.hotelsystem.common;

import io.github.wojtekolo.hotelsystem.common.person.PersonTestUtils;
import io.github.wojtekolo.hotelsystem.customer.CustomerTestUtils;
import io.github.wojtekolo.hotelsystem.customer.model.Customer;
import io.github.wojtekolo.hotelsystem.customer.model.LoyaltyStatus;
import io.github.wojtekolo.hotelsystem.employee.EmployeeTestUtils;
import io.github.wojtekolo.hotelsystem.employee.model.Employee;
import io.github.wojtekolo.hotelsystem.person.model.Person;
import io.github.wojtekolo.hotelsystem.room.RoomTestUtils;
import io.github.wojtekolo.hotelsystem.room.model.Room;
import io.github.wojtekolo.hotelsystem.room.model.RoomType;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@Transactional
@RequiredArgsConstructor
public class TestDataFactory {
    private final EntityManager entityManager;

    public Room prepareRoom(BigDecimal pricePerNight) {
        RoomType roomType = RoomTestUtils.aValidType().pricePerNight(pricePerNight).build();
        entityManager.persist(roomType);
        Room room = RoomTestUtils.aValidRoom(roomType).build();
        entityManager.persist(room);
        return room;
    }

    public Room prepareRoom() {
        return prepareRoom(BigDecimal.valueOf(15));
    }

    public Customer prepareCustomer() {
        return prepareCustomer(BigDecimal.ZERO);
    }

    public Customer prepareCustomer(BigDecimal discount) {
        Person person = PersonTestUtils.aValidPerson().build();
        entityManager.persist(person);

        LoyaltyStatus loyaltyStatus = CustomerTestUtils.aValidLoyaltyStatus().discount(discount).build();
        entityManager.persist(loyaltyStatus);

        Customer customer = CustomerTestUtils.aValidCustomer(person, loyaltyStatus).build();
        entityManager.persist(customer);
        return customer;
    }

    public Employee prepareEmployee() {
        Person person = PersonTestUtils.aValidPerson().build();
        entityManager.persist(person);

        Employee employee = EmployeeTestUtils.aValidEmployee(person).build();
        entityManager.persist(employee);
        return employee;
    }
}
