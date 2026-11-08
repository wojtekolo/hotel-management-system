package io.github.wojtekolo.hotelsystem.booking.service;

import io.github.wojtekolo.hotelsystem.AbstractIntegrationTest;
import io.github.wojtekolo.hotelsystem.booking.api.request.BookingCreateRequest;
import io.github.wojtekolo.hotelsystem.booking.api.request.RoomStayCreateRequest;
import io.github.wojtekolo.hotelsystem.booking.api.request.RoomStayUpdateRequest;
import io.github.wojtekolo.hotelsystem.booking.api.response.BookingDetails;
import io.github.wojtekolo.hotelsystem.booking.api.request.BookingUpdateRequest;
import io.github.wojtekolo.hotelsystem.booking.api.response.RoomStayDetails;
import io.github.wojtekolo.hotelsystem.booking.exception.BookingValidationException;
import io.github.wojtekolo.hotelsystem.booking.exception.details.IntegrityErrorCode;
import io.github.wojtekolo.hotelsystem.booking.exception.details.IntegrityViolationDetails;
import io.github.wojtekolo.hotelsystem.booking.exception.details.RoomStayViolationCode;
import io.github.wojtekolo.hotelsystem.booking.exception.details.RoomStayViolationDetails;
import io.github.wojtekolo.hotelsystem.booking.model.entity.*;
import io.github.wojtekolo.hotelsystem.booking.persistence.BookingRepository;
import io.github.wojtekolo.hotelsystem.booking.persistence.RoomStayRepository;
import io.github.wojtekolo.hotelsystem.common.TestDataFactory;
import io.github.wojtekolo.hotelsystem.customer.model.Customer;
import io.github.wojtekolo.hotelsystem.employee.model.Employee;
import io.github.wojtekolo.hotelsystem.room.model.Room;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static io.github.wojtekolo.hotelsystem.booking.BookingTestUtils.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@Testcontainers
class BookingServiceIntegrationTest extends AbstractIntegrationTest {

    private final LocalDate today = LocalDate.now();

    @Autowired
    BookingService bookingService;

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    RoomStayRepository roomStayRepository;

    @Autowired
    EntityManager entityManager;

    @Autowired
    TestDataFactory data;

    @Test
    public void should_persist_single_stay() {
//        given
        Room room = data.prepareRoom();
        Customer customer = data.prepareCustomer();

        var bookingCreateRequest = new BookingCreateRequest(customer.getId(), List.of(
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
        assertThat(savedBooking.getStatus()).isEqualTo(BookingStatus.PLANNED);
        assertThat(savedBooking.getCreateBy()).isNull();
        assertThat(savedBooking.getStays().getFirst().getCreateBy()).isNull();

        assertThat(result.customerFullName()).isEqualTo(customer.getPerson().getFullName());
        assertThat(result.createBy()).isNull();
        assertThat(result.stays()).hasSize(1);
        assertThat(result.stays().getFirst().roomName()).isEqualTo(room.getName());
        assertThat(result.stays().getFirst().createBy()).isNull();
        assertThat(result.stays().getFirst().checkInBy()).isNull();
    }

    @Test
    public void should_calculate_correct_cost_when_default_room_price_and_discount() {
//        given
        Room room1 = data.prepareRoom(BigDecimal.valueOf(100));
        Room room2 = data.prepareRoom(BigDecimal.valueOf(10));
        Customer customer = data.prepareCustomer(BigDecimal.valueOf(0.5));

        var bookingCreateRequest = new BookingCreateRequest(customer.getId(), List.of(
                new RoomStayCreateRequest(room1.getId(), today.plusDays(5), today.plusDays(10)),
                new RoomStayCreateRequest(room2.getId(), today.plusDays(5), today.plusDays(10))
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

    @Test
    public void should_return_all_violations_when_invalid_create_request() {
//        given
        Long invalidRoomId = 100L;
        Long invalidCustomerId = 200L;
        LocalDate invalidStartDate = today.minusDays(5);
        LocalDate invalidEndDate = today.minusDays(10);

        BookingCreateRequest createRequest = new BookingCreateRequest(
                invalidCustomerId,
                List.of(new RoomStayCreateRequest(invalidRoomId, invalidStartDate, invalidEndDate)
                )
        );

//        when and then
        entityManager.flush();
        entityManager.clear();

        assertThatThrownBy(() -> bookingService.addBooking(createRequest))
                .isInstanceOf(BookingValidationException.class)
                .satisfies(ex -> {
                    BookingValidationException roomStayEx = (BookingValidationException) ex;
                    assertThat(roomStayEx.getRoomStayViolationsDetails())
                            .extracting(RoomStayViolationDetails::code)
                            .containsExactlyInAnyOrder(
                                    RoomStayViolationCode.END_DATE_IN_THE_PAST,
                                    RoomStayViolationCode.START_DATE_IN_THE_PAST,
                                    RoomStayViolationCode.END_DATE_NOT_AFTER_START_DATE
                            );
                    assertThat(roomStayEx.getIntegrityViolationsDetails())
                            .extracting(IntegrityViolationDetails::code)
                            .containsExactlyInAnyOrder(
                                    IntegrityErrorCode.CUSTOMER_NOT_FOUND,
                                    IntegrityErrorCode.ROOM_NOT_FOUND
                            );
                });
        assertThat(bookingRepository.count()).isEqualTo(0);
        assertThat(roomStayRepository.count()).isEqualTo(0);
    }

    @Test
    public void should_return_all_violations_when_invalid_update_request() {
//        given
        Room room = data.prepareRoom();
        Employee employee = data.prepareEmployee();
        Customer customer = data.prepareCustomer();

        Booking existingBooking = aValidBooking(customer, employee).build();
        RoomStay existingStay = aValidRoomStay(existingBooking, room, employee)
                .activeFrom(today.plusDays(10)).activeTo(today.plusDays(15)).build();
        existingBooking.addStay(existingStay);

        entityManager.persist(existingBooking);
        entityManager.flush();
        entityManager.clear();


        Long invalidRoomId = 100L;
        LocalDate invalidStartDate = today.minusDays(5);
        LocalDate invalidEndDate = today.minusDays(10);

        BookingUpdateRequest updateRequest = new BookingUpdateRequest(
                List.of(new RoomStayUpdateRequest(existingStay.getId(), invalidRoomId, invalidStartDate,
                        invalidEndDate)
                )
        );

//        when and then
        entityManager.flush();
        entityManager.clear();

        assertThatThrownBy(() -> bookingService.updateBooking(existingBooking.getId(), updateRequest))
                .isInstanceOf(BookingValidationException.class)
                .satisfies(ex -> {
                    BookingValidationException roomStayEx = (BookingValidationException) ex;
                    assertThat(roomStayEx.getRoomStayViolationsDetails())
                            .extracting(RoomStayViolationDetails::code)
                            .containsExactlyInAnyOrder(
                                    RoomStayViolationCode.END_DATE_IN_THE_PAST,
                                    RoomStayViolationCode.START_DATE_IN_THE_PAST,
                                    RoomStayViolationCode.END_DATE_NOT_AFTER_START_DATE
                            );
                    assertThat(roomStayEx.getIntegrityViolationsDetails())
                            .extracting(IntegrityViolationDetails::code)
                            .containsExactlyInAnyOrder(
                                    IntegrityErrorCode.ROOM_NOT_FOUND
                            );
                });
    }

    @Test
    public void should_not_return_other_stay_violations_when_invalid_stay_id() {
//        given
        Room room = data.prepareRoom();
        Employee employee = data.prepareEmployee();
        Customer customer = data.prepareCustomer();

        Booking booking = aValidBooking(customer, employee).build();
        RoomStay stay1 = aValidRoomStay(booking, room, employee)
                .status(RoomStayStatus.CANCELLED)
                .activeFrom(today.plusDays(10)).activeTo(today.plusDays(15)).build();

        RoomStay stay2 = aValidRoomStay(booking, room, employee)
                .activeFrom(today.plusDays(10)).activeTo(today.plusDays(15)).build();

        booking.addStay(stay1);
        booking.addStay(stay2);
        entityManager.persist(booking);
        entityManager.flush();
        entityManager.clear();

        Long invalidStayId = 100L;

        BookingUpdateRequest updateRequest = new BookingUpdateRequest(
                List.of(
                        new RoomStayUpdateRequest(stay1.getId(), room.getId(),
                                stay1.getActiveFrom(), stay1.getActiveTo().plusDays(2)),
                        new RoomStayUpdateRequest(invalidStayId, room.getId(),
                                stay2.getActiveFrom(), stay2.getActiveTo())
                )
        );

//        when and then
        assertThatThrownBy(() -> bookingService.updateBooking(booking.getId(), updateRequest))
                .isInstanceOf(BookingValidationException.class)
                .satisfies(ex -> {
                    BookingValidationException roomStayEx = (BookingValidationException) ex;
                    assertThat(roomStayEx.getRoomStayViolationsDetails())
                            .extracting(RoomStayViolationDetails::code)
                            .containsExactlyInAnyOrder(
                                    RoomStayViolationCode.STAY_NOT_FOUND_IN_BOOKING
                            );
                });
    }

    @Test
    public void should_return_all_violations_when_stay_status_invalid_for_edit() {
//        given
        Room room = data.prepareRoom();
        Employee employee = data.prepareEmployee();
        Customer customer = data.prepareCustomer();

        Booking existingBooking = aValidBooking(customer, employee).build();
        RoomStay existingStay = aValidRoomStay(existingBooking, room, employee)
                .status(RoomStayStatus.COMPLETED)
                .activeFrom(today.plusDays(10)).activeTo(today.plusDays(15)).build();
        existingBooking.addStay(existingStay);

        entityManager.persist(existingBooking);
        entityManager.flush();
        entityManager.clear();

        Room newRoom = data.prepareRoom();
        LocalDate newStart = today.plusDays(20);
        LocalDate newEnd = today.plusDays(25);

        BookingUpdateRequest updateRequest = new BookingUpdateRequest(
                List.of(new RoomStayUpdateRequest(existingStay.getId(), newRoom.getId(), newStart, newEnd)
                )
        );

//        when and then
        entityManager.flush();
        entityManager.clear();

        assertThatThrownBy(() -> bookingService.updateBooking(existingBooking.getId(), updateRequest))
                .isInstanceOf(BookingValidationException.class)
                .satisfies(ex -> {
                    BookingValidationException roomStayEx = (BookingValidationException) ex;
                    assertThat(roomStayEx.getRoomStayViolationsDetails())
                            .extracting(RoomStayViolationDetails::code)
                            .containsExactlyInAnyOrder(
                                    RoomStayViolationCode.ROOM_EDIT_INVALID_STATUS,
                                    RoomStayViolationCode.START_DATE_EDIT_INVALID_STATUS,
                                    RoomStayViolationCode.END_DATE_EDIT_INVALID_STATUS
                            );

                    assertThat(roomStayEx.getIntegrityViolationsDetails())
                            .extracting(IntegrityViolationDetails::code)
                            .isEmpty();
                });
    }

    @Test
    public void should_return_all_violations_when_stay_status_invalid_for_cancel() {
//        given
        Room room = data.prepareRoom();
        Employee employee = data.prepareEmployee();
        Customer customer = data.prepareCustomer();

        Booking existingBooking = aValidBooking(customer, employee).build();
        RoomStay existingStay = aValidRoomStay(existingBooking, room, employee)
                .status(RoomStayStatus.COMPLETED)
                .activeFrom(today.plusDays(10)).activeTo(today.plusDays(15)).build();
        existingBooking.addStay(existingStay);

        entityManager.persist(existingBooking);
        entityManager.flush();
        entityManager.clear();

        BookingUpdateRequest updateRequest = new BookingUpdateRequest(
                List.of()
        );

//        when and then
        assertThatThrownBy(() -> bookingService.updateBooking(existingBooking.getId(), updateRequest))
                .isInstanceOf(BookingValidationException.class)
                .satisfies(ex -> {
                    BookingValidationException roomStayEx = (BookingValidationException) ex;
                    assertThat(roomStayEx.getRoomStayViolationsDetails())
                            .extracting(RoomStayViolationDetails::code)
                            .containsExactlyInAnyOrder(
                                    RoomStayViolationCode.CANCEL_INVALID_STATUS
                            );
                });
    }


}