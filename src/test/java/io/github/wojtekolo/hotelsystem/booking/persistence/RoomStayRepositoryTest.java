package io.github.wojtekolo.hotelsystem.booking.persistence;

import io.github.wojtekolo.hotelsystem.booking.BookingTestContext;
import io.github.wojtekolo.hotelsystem.booking.BookingTestUtils;
import io.github.wojtekolo.hotelsystem.booking.model.entity.Booking;
import io.github.wojtekolo.hotelsystem.booking.model.entity.RoomStay;
import io.github.wojtekolo.hotelsystem.booking.model.entity.RoomStayStatus;
import io.github.wojtekolo.hotelsystem.person.model.Person;
import io.github.wojtekolo.hotelsystem.common.person.PersonTestUtils;
import io.github.wojtekolo.hotelsystem.customer.model.Customer;
import io.github.wojtekolo.hotelsystem.customer.CustomerTestUtils;
import io.github.wojtekolo.hotelsystem.customer.model.LoyaltyStatus;
import io.github.wojtekolo.hotelsystem.employee.model.Employee;
import io.github.wojtekolo.hotelsystem.employee.EmployeeTestUtils;
import io.github.wojtekolo.hotelsystem.room.model.Room;
import io.github.wojtekolo.hotelsystem.room.RoomTestUtils;
import io.github.wojtekolo.hotelsystem.room.model.RoomType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class RoomStayRepositoryTest {
    @Autowired
    RoomStayRepository roomStayRepository;

    @Autowired
    EntityManager entityManager;

    @Test
    public void should_not_find_conflicts_when_exactly_between_existing_stays() {
//        given
        LocalDate today = LocalDate.now();
        BookingTestContext context = getContext();

        Booking booking = BookingTestUtils.aValidBooking(context.customer(), context.employee()).build();

        RoomStay roomStay1 = BookingTestUtils.aValidRoomStay(booking, context.room(), context.employee())
                .activeFrom(today.plusDays(10))
                .activeTo(today.plusDays(12))
                .build();

        RoomStay roomStay2 = BookingTestUtils.aValidRoomStay(booking, context.room(), context.employee())
                .activeFrom(today.plusDays(17))
                .activeTo(today.plusDays(20))
                .build();

        booking.addStay(roomStay1);
        booking.addStay(roomStay2);

        entityManager.persist(booking);

        entityManager.flush();
//        when
        List<RoomStay> result = roomStayRepository.getConflicts(context.room().getId(),
                List.of(RoomStayStatus.ACTIVE, RoomStayStatus.PLANNED),
                today.plusDays(12),
                today.plusDays(17));

//        then
        assertThat(result).isEmpty();
    }

    @Test
    public void should_find_multiple_conflicts_when_overlap() {
//        given
        LocalDate today = LocalDate.now();
        BookingTestContext context = getContext();

        Booking booking = BookingTestUtils.aValidBooking(context.customer(), context.employee()).build();

        RoomStay roomStay1 = BookingTestUtils.aValidRoomStay(booking, context.room(), context.employee())
                .activeFrom(today.plusDays(10))
                .activeTo(today.plusDays(15))
                .build();

        RoomStay roomStay2 = BookingTestUtils.aValidRoomStay(booking, context.room(), context.employee())
                .activeFrom(today.plusDays(15))
                .activeTo(today.plusDays(20))
                .build();

        booking.addStay(roomStay1);
        booking.addStay(roomStay2);

        entityManager.persist(booking);

        entityManager.flush();
//        when
        List<RoomStay> result = roomStayRepository.getConflicts(context.room().getId(),
                List.of(RoomStayStatus.ACTIVE, RoomStayStatus.PLANNED),
                today.plusDays(12),
                today.plusDays(17));

//        then
        assertThat(result)
                .hasSize(2)
                .extracting(RoomStay::getId)
                .containsExactlyInAnyOrder(roomStay1.getId(), roomStay2.getId());

    }

    private BookingTestContext getContext() {
        RoomType roomType = RoomTestUtils.aValidType().build();
        entityManager.persist(roomType);

        Room room = RoomTestUtils.aValidRoom(roomType)
                .build();
        entityManager.persist(room);

        Person employeePerson = PersonTestUtils.aValidPerson().build();
        Person customerPerson = PersonTestUtils.aValidPerson().build();

        entityManager.persist(employeePerson);
        entityManager.persist(customerPerson);

        LoyaltyStatus loyaltyStatus = CustomerTestUtils.aValidLoyaltyStatus().build();
        entityManager.persist(loyaltyStatus);

        Customer customer = CustomerTestUtils.aValidCustomer(customerPerson, loyaltyStatus).build();
        entityManager.persist(customer);

        Employee employee = EmployeeTestUtils.aValidEmployee(employeePerson).build();
        entityManager.persist(employee);
        return new BookingTestContext(customer, employee, room);
    }

}