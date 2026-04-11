package io.github.wojtekolo.hotelsystem.common;

import io.github.wojtekolo.hotelsystem.booking.api.request.BookingCreateRequest;
import io.github.wojtekolo.hotelsystem.booking.api.request.BookingUpdateRequest;
import io.github.wojtekolo.hotelsystem.booking.api.request.RoomStayUpdateRequest;
import io.github.wojtekolo.hotelsystem.booking.model.entity.Booking;
import io.github.wojtekolo.hotelsystem.booking.model.entity.RoomStay;
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
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static io.github.wojtekolo.hotelsystem.booking.BookingTestUtils.*;

@Component
@Transactional
@RequiredArgsConstructor
public class TestDataFactory {
    private final LocalDate today = LocalDate.now();
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

    public Map<Long, BookingUpdateRequest> prepareCollidingUpdateRequests(int count, Room targetRoom, Customer customer, Employee employee) {
        Map<Long, BookingUpdateRequest> requests = new HashMap<>();
        IntStream.range(0, count).forEach(i -> {

            Room room = prepareRoom();
            Booking booking = aValidBooking(customer, employee).build();
            RoomStay stay = aValidRoomStay(booking, room, employee)
                    .activeFrom(today.plusDays(10)).activeTo(today.plusDays(15)).build();
            booking.addStay(stay);

            entityManager.persist(booking);

            requests.put(booking.getId(), new BookingUpdateRequest(employee.getId(), List.of(
                    new RoomStayUpdateRequest(stay.getId(), targetRoom.getId(), stay.getActiveFrom(), stay.getActiveTo(), null)
            )));

        });
        return requests;
    }

    public List<BookingCreateRequest> prepareCollidingCreateRequests(int count, Room room, Customer customer, Employee employee) {
        return IntStream.range(0, count)
                .mapToObj(
                        i -> new BookingCreateRequest(customer.getId(), employee.getId(), List.of(
                                createRoomStayCreateRequest(room.getId(), today.plusDays(5), today.plusDays(10))))
                )
                .toList();
    }
}
