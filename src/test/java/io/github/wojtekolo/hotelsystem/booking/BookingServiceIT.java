package io.github.wojtekolo.hotelsystem.booking;

import io.github.wojtekolo.hotelsystem.booking.api.BookingCreateRequest;
import io.github.wojtekolo.hotelsystem.booking.api.BookingDetails;
import io.github.wojtekolo.hotelsystem.booking.api.BookingUpdateRequest;
import io.github.wojtekolo.hotelsystem.booking.api.RoomStayDetails;
import io.github.wojtekolo.hotelsystem.booking.model.Booking;
import io.github.wojtekolo.hotelsystem.booking.model.BookingStatus;
import io.github.wojtekolo.hotelsystem.booking.model.PaymentStatus;
import io.github.wojtekolo.hotelsystem.booking.model.RoomStay;
import io.github.wojtekolo.hotelsystem.booking.persistence.BookingRepository;
import io.github.wojtekolo.hotelsystem.booking.service.BookingService;
import io.github.wojtekolo.hotelsystem.common.person.Person;
import io.github.wojtekolo.hotelsystem.common.person.PersonTestUtils;
import io.github.wojtekolo.hotelsystem.customer.*;
import io.github.wojtekolo.hotelsystem.employee.Employee;
import io.github.wojtekolo.hotelsystem.employee.EmployeeTestUtils;
import io.github.wojtekolo.hotelsystem.room.*;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static io.github.wojtekolo.hotelsystem.booking.BookingTestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@SpringBootTest
@Transactional
@TestPropertySource(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class BookingServiceIT {

    private final LocalDate today = LocalDate.now();

    @Autowired
    BookingService bookingService;

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    EntityManager entityManager;

    @Test
    public void should_persist_single_stay() {
//        given
        Room room = prepareRoom();
        Employee employee = prepareEmployee();
        Customer customer = prepareCustomer();

        var bookingCreateRequest = new BookingCreateRequest(customer.getId(), employee.getId(), List.of(
                createRoomStayCreateRequest(room.getId(), today.plusDays(5), today.plusDays(10))
        ));

//        when
        BookingDetails result = bookingService.addBooking(bookingCreateRequest);

//        then
        Booking savedBooking = bookingRepository.findById(result.id())
                                                .orElseThrow(() -> new AssertionError("Booking not found in database"));

        assertThat(savedBooking.getId()).isNotNull();
        assertThat(savedBooking.getStays()).hasSize(1);
        assertThat(savedBooking.getCustomer().getId()).isEqualTo(customer.getId());
        assertThat(savedBooking.getStays().getFirst().getRoom().getId()).isEqualTo(room.getId());
        assertThat(savedBooking.getPaymentStatus()).isEqualTo(PaymentStatus.UNPAID);
        assertThat(savedBooking.getStatus()).isEqualTo(BookingStatus.PLANNED);
        assertThat(savedBooking.getCreateBy().getId()).isEqualTo(employee.getId());

        assertThat(result.customerFullName()).isEqualTo(customer.getPerson().getFullName());
        assertThat(result.stays()).hasSize(1);
        assertThat(result.stays().getFirst().roomName()).isEqualTo(room.getName());
        assertThat(result.stays().getFirst().createBy()).isEqualTo(employee.getPerson().getFullName());
        assertThat(result.stays().getFirst().checkInBy()).isNull();
    }

    @Test
    public void should_calculate_correct_cost_when_default_price_and_discount() {
//        given
        Room room1 = prepareRoom(BigDecimal.valueOf(100));
        Room room2 = prepareRoom(BigDecimal.valueOf(10));
        Employee employee = prepareEmployee();
        Customer customer = prepareCustomer(BigDecimal.valueOf(0.5));

        var bookingCreateRequest = new BookingCreateRequest(customer.getId(), employee.getId(), List.of(
                createRoomStayCreateRequest(room1.getId(), today.plusDays(5), today.plusDays(10)),
                createRoomStayCreateRequest(room2.getId(), today.plusDays(5), today.plusDays(10))
        ));

//        when
        BookingDetails result = bookingService.addBooking(bookingCreateRequest);

//        then
        assertThat(result.totalCost()).isEqualByComparingTo(BigDecimal.valueOf(275));

        assertThat(result.stays())
                .extracting(RoomStayDetails::roomId, RoomStayDetails::totalCost)
                .containsExactlyInAnyOrder(
                        tuple(room1.getId(), new BigDecimal("250.00")),
                        tuple(room2.getId(), new BigDecimal("25.00"))
                );
    }

    @Test
    public void should_calculate_correct_cost_when_custom_price() {
//        given
        Room room1 = prepareRoom(BigDecimal.valueOf(100));
        Room room2 = prepareRoom(BigDecimal.valueOf(10));
        Employee employee = prepareEmployee();
        Customer customer = prepareCustomer(BigDecimal.valueOf(0.5));

        var bookingCreateRequest = new BookingCreateRequest(customer.getId(), employee.getId(), List.of(
                createRoomStayCreateRequest(room1.getId(), today.plusDays(5), today.plusDays(10), BigDecimal.valueOf(30)),
                createRoomStayCreateRequest(room2.getId(), today.plusDays(5), today.plusDays(10), BigDecimal.valueOf(40))
        ));

//        when
        BookingDetails result = bookingService.addBooking(bookingCreateRequest);

//        then
        assertThat(result.totalCost()).isEqualByComparingTo(BigDecimal.valueOf(350));

        assertThat(result.stays())
                .extracting(RoomStayDetails::roomId, RoomStayDetails::totalCost)
                .containsExactlyInAnyOrder(
                        tuple(room1.getId(), new BigDecimal("150.00")),
                        tuple(room2.getId(), new BigDecimal("200.00"))
                );
    }

    @Test
    public void should_update_ignoring_self_collision() {
//        given
        Room room = prepareRoom();
        Employee employee = prepareEmployee();
        Customer customer = prepareCustomer();

        Booking booking = aValidBooking(customer, employee).build();
        RoomStay stay = aValidRoomStay(booking, room, employee)
                .activeFrom(today.plusDays(10)).activeTo(today.plusDays(15)).build();
        booking.addStay(stay);
        entityManager.persist(booking);

        BookingUpdateRequest updateRequest = createBookingUpdateRequest(
                booking.getId(),
                employee.getId(),
                List.of(
                        createRoomStayUpdateRequest(booking.getStays().getFirst().getId(),
                                room.getId(), today.plusDays(11), today.plusDays(16))
                )
        );

//        when
        BookingDetails result = bookingService.updateBooking(updateRequest);

//        then
        Booking savedBooking = bookingRepository.findById(result.id())
                                                .orElseThrow(() -> new AssertionError("Booking not found in database"));

        assertThat(result.stays().getFirst().activeFrom()).isEqualTo(today.plusDays(11));
        assertThat(result.stays().getFirst().activeTo()).isEqualTo(today.plusDays(16));
    }

    private Room prepareRoom(BigDecimal pricePerNight) {
        RoomType roomType = RoomTestUtils.aValidType().pricePerNight(pricePerNight).build();
        entityManager.persist(roomType);
        Room room = RoomTestUtils.aValidRoom(roomType).build();
        entityManager.persist(room);
        return room;
    }

    private Room prepareRoom() {
        return prepareRoom(BigDecimal.valueOf(15));
    }

    private Customer prepareCustomer() {
        return prepareCustomer(BigDecimal.ZERO);
    }

    private Customer prepareCustomer(BigDecimal discount) {
        Person person = PersonTestUtils.aValidPerson().build();
        entityManager.persist(person);

        LoyaltyStatus loyaltyStatus = CustomerTestUtils.aValidLoyaltyStatus().discount(discount).build();
        entityManager.persist(loyaltyStatus);

        Customer customer = CustomerTestUtils.aValidCustomer(person, loyaltyStatus).build();
        entityManager.persist(customer);
        return customer;
    }

    private Employee prepareEmployee() {
        Person person = PersonTestUtils.aValidPerson().build();
        entityManager.persist(person);

        Employee employee = EmployeeTestUtils.aValidEmployee(person).build();
        entityManager.persist(employee);
        return employee;
    }
}