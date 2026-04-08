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
import io.github.wojtekolo.hotelsystem.common.TestDataFactory;
import io.github.wojtekolo.hotelsystem.customer.model.Customer;
import io.github.wojtekolo.hotelsystem.employee.model.Employee;
import io.github.wojtekolo.hotelsystem.room.model.Room;
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
class BookingServiceIntegrationTest {

    private final LocalDate today = LocalDate.now();

    @Autowired
    BookingService bookingService;

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    EntityManager entityManager;

    @Autowired
    TestDataFactory data;

    @Test
    public void should_persist_single_stay() {
//        given
        Room room = data.prepareRoom();
        Employee employee = data.prepareEmployee();
        Customer customer = data.prepareCustomer();

        var bookingCreateRequest = new BookingCreateRequest(customer.getId(), employee.getId(), List.of(
                createRoomStayCreateRequest(room.getId(), today.plusDays(5), today.plusDays(10))
        ));

//        when
        BookingDetails result = bookingService.addBooking(bookingCreateRequest);

//        then
        entityManager.flush();
        entityManager.clear();
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
        Room room1 = data.prepareRoom(BigDecimal.valueOf(100));
        Room room2 = data.prepareRoom(BigDecimal.valueOf(10));
        Employee employee = data.prepareEmployee();
        Customer customer = data.prepareCustomer(BigDecimal.valueOf(0.5));

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
        Room room1 = data.prepareRoom(BigDecimal.valueOf(100));
        Room room2 = data.prepareRoom(BigDecimal.valueOf(10));
        Employee employee = data.prepareEmployee();
        Customer customer = data.prepareCustomer(BigDecimal.valueOf(0.5));

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
        Room room = data.prepareRoom();
        Employee employee = data.prepareEmployee();
        Customer customer = data.prepareCustomer();

        Booking booking = aValidBooking(customer, employee).build();
        RoomStay stay = aValidRoomStay(booking, room, employee)
                .activeFrom(today.plusDays(10)).activeTo(today.plusDays(15)).build();
        booking.addStay(stay);
        entityManager.persist(booking);
        entityManager.flush();
        entityManager.clear();

        BookingUpdateRequest updateRequest = createBookingUpdateRequest(
                employee.getId(),
                List.of(
                        createRoomStayUpdateRequest(booking.getStays().getFirst().getId(),
                                room.getId(), today.plusDays(11), today.plusDays(16))
                )
        );

//        when
        BookingDetails result = bookingService.updateBooking(booking.getId(), updateRequest);

//        then
        entityManager.flush();
        entityManager.clear();
        assertThat(result.id()).isEqualTo(booking.getId());
        Booking savedBooking = bookingRepository.findById(result.id())
                                                .orElseThrow(() -> new AssertionError("Booking not found in database"));

        assertThat(savedBooking.getStays().getFirst().getActiveFrom()).isEqualTo(today.plusDays(11));
        assertThat(savedBooking.getStays().getFirst().getActiveTo()).isEqualTo(today.plusDays(16));
    }


}