package io.github.wojtekolo.hotelsystem.booking;

import io.github.wojtekolo.hotelsystem.booking.api.BookingCreateRequest;
import io.github.wojtekolo.hotelsystem.booking.api.BookingDetails;
import io.github.wojtekolo.hotelsystem.booking.api.RoomStayBadStatusDetails;
import io.github.wojtekolo.hotelsystem.booking.api.RoomStayDetails;
import io.github.wojtekolo.hotelsystem.booking.exception.BookingValidationException;
import io.github.wojtekolo.hotelsystem.booking.exception.RoomStayErrorCode;
import io.github.wojtekolo.hotelsystem.booking.model.Booking;
import io.github.wojtekolo.hotelsystem.booking.model.RoomStay;
import io.github.wojtekolo.hotelsystem.booking.model.RoomStayStatus;
import io.github.wojtekolo.hotelsystem.booking.persistence.BookingRepository;
import io.github.wojtekolo.hotelsystem.booking.persistence.RoomStayRepository;
import io.github.wojtekolo.hotelsystem.booking.service.BookingMapper;
import io.github.wojtekolo.hotelsystem.booking.service.BookingService;
import io.github.wojtekolo.hotelsystem.booking.service.BookingValidator;
import io.github.wojtekolo.hotelsystem.booking.service.BookingMapperImpl;
import io.github.wojtekolo.hotelsystem.common.exceptions.*;
import io.github.wojtekolo.hotelsystem.common.person.PersonTestUtils;
import io.github.wojtekolo.hotelsystem.customer.*;
import io.github.wojtekolo.hotelsystem.customer.model.Customer;
import io.github.wojtekolo.hotelsystem.customer.persistence.CustomerRepository;
import io.github.wojtekolo.hotelsystem.customer.service.CustomerMapper;
import io.github.wojtekolo.hotelsystem.employee.model.Employee;
import io.github.wojtekolo.hotelsystem.employee.service.EmployeeMapper;
import io.github.wojtekolo.hotelsystem.employee.persistence.EmployeeRepository;
import io.github.wojtekolo.hotelsystem.employee.EmployeeTestUtils;
import io.github.wojtekolo.hotelsystem.room.*;
import io.github.wojtekolo.hotelsystem.room.model.Room;
import io.github.wojtekolo.hotelsystem.room.persistence.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static io.github.wojtekolo.hotelsystem.booking.BookingTestUtils.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private CustomerMapper customerMapper;

    @Mock
    private EmployeeMapper employeeMapper;


    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RoomStayRepository roomStayRepository;

    @Mock
    private BookingValidator bookingValidator;

    private BookingService bookingService;

    private final LocalDate today = LocalDate.now();


    private final Long employeeId = 1L;
    private final Long customerId = 20L;

    @BeforeEach
    void setUp() {
        BookingMapper bookingMapper = new BookingMapperImpl(customerMapper, employeeMapper);
        bookingService = new BookingService(
                bookingMapper,
                employeeRepository,
                roomRepository,
                customerRepository,
                bookingRepository,
                bookingValidator
        );
    }

    @Test
    public void should_add_booking_when_data_is_valid() {
        mockRoom(1L);
        mockRoom(2L);
        mockEmployee();
        mockCustomer();
        mockBooking();

        var request = createBookingCreateRequest(List.of(
           createRoomStayCreateRequest(1L, today.plusDays(10), today.plusDays(15)),
           createRoomStayCreateRequest(2L, today.plusDays(10), today.plusDays(15))
        ));
//        when
        bookingService.addBooking(request);

//        then
        verify(bookingRepository, times(1)).save(any());
        verify(bookingValidator, times(1)).validateInternalConflicts(any());
        verify(bookingValidator, times(1)).validateExternalConflicts(any());
    }

    @Test
    public void should_throw_resource_not_found_when_invalid_employee() {
//        given
        var request = new BookingCreateRequest(
                customerId,
                employeeId + 1,
                List.of(
                        createRoomStayCreateRequest(1L, today.plusDays(5), today.plusDays(10))
                )
        );
        assertThatThrownBy(() ->
                bookingService.addBooking(request)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Employee")
                .hasMessageContaining(String.valueOf(employeeId + 1));
    }

    @Test
    public void should_throw_resource_not_found_when_invalid_customer() {
        mockEmployee();

        var request = new BookingCreateRequest(
                customerId + 1,
                employeeId,
                List.of(
                        createRoomStayCreateRequest(1L, today.plusDays(5), today.plusDays(10))
                )
        );

        assertThatThrownBy(() ->
                bookingService.addBooking(request)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Customer")
                .hasMessageContaining(String.valueOf(customerId + 1));
    }

    @Test
    public void should_throw_resource_not_found_when_invalid_room() {
        mockEmployee();
        mockCustomer();

        BookingCreateRequest request = createBookingCreateRequest(List.of(
                createRoomStayCreateRequest(
                        15L,
                        today.plusDays(5),
                        today.plusDays(10)
                )
        ));

        assertThatThrownBy(() ->
                bookingService.addBooking(request)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Room")
                .hasMessageContaining(String.valueOf(15L));
    }



//    -------------UPDATE TESTS-------------

    @Test
    public void should_update_single_stay() {
//        given
        Customer customer = CustomerTestUtils.aValidCustomer().build();
        Employee employee = mockEmployee();
        mockBooking();

        Room room = mockRoom(2L, "roomname2");


        Booking booking = BookingTestUtils.aValidBooking(customer, employee).id(1L).build();
        booking.addStay(buildStay(1L, room, employee, today.plusDays(10), today.plusDays(20)));
        mockBooking(booking);

        var newBookingRequest = createBookingUpdateRequest(1L, employeeId, List.of(
                createRoomStayUpdateRequest(1L, 2L, today.plusDays(12), today.plusDays(15))
        ));

//        when
        BookingDetails details = bookingService.updateBooking(newBookingRequest);

//        then
        assertThat(details.stays().getFirst().roomId()).isEqualTo(2L);
        assertThat(details.stays().getFirst().roomName()).isEqualTo("roomname2");
        assertThat(details.stays().getFirst().activeFrom()).isEqualTo(today.plusDays(12));
        assertThat(details.stays().getFirst().activeTo()).isEqualTo(today.plusDays(15));
    }

    @Test
    public void should_update_multiple_stays_for_the_same_room() {
//        given
        Customer customer = CustomerTestUtils.aValidCustomer().build();
        Employee employee = mockEmployee();
        mockBooking();

        Room room = mockRoom(1L);

        Booking booking = BookingTestUtils.aValidBooking(customer, employee).id(1L).build();
        booking.addStay(buildStay(1L, room, employee, today.plusDays(10), today.plusDays(15)));
        booking.addStay(buildStay(2L, room, employee, today.plusDays(15), today.plusDays(20))); //asdas
        mockBooking(booking);

        var newBookingRequest = createBookingUpdateRequest(1L, employeeId, List.of(
                createRoomStayUpdateRequest(1L, 1L, today.plusDays(11), today.plusDays(16)),
                createRoomStayUpdateRequest(2L, 1L, today.plusDays(16), today.plusDays(21)) //asdas
        ));

//        when
        BookingDetails details = bookingService.updateBooking(newBookingRequest);

//        then
        assertThat(details.stays())
                .extracting(RoomStayDetails::activeFrom)
                .containsExactlyInAnyOrder(today.plusDays(11), today.plusDays(16));

        assertThat(details.stays())
                .extracting(RoomStayDetails::activeTo)
                .containsExactlyInAnyOrder(today.plusDays(21),today.plusDays(16));
    }

    @Test
    public void should_change_status_to_cancelled_when_deleting_stay() {
//        given
        Customer customer = CustomerTestUtils.aValidCustomer().build();
        Employee employee = mockEmployee();
        mockBooking();

        Room room = mockRoom(1L);

        Booking booking = BookingTestUtils.aValidBooking(customer, employee).id(1L).build();

        booking.addStay(buildStay(1L, room, employee, today.plusDays(10), today.plusDays(15)));
        RoomStay stayToBeRemoved = buildStay(2L, room, employee, today.plusDays(15), today.plusDays(20));
        booking.addStay(stayToBeRemoved);

        mockBooking(booking);

        var newBookingRequest = createBookingUpdateRequest(1L, employeeId, List.of(
                createRoomStayUpdateRequest(1L, 1L, today.plusDays(11), today.plusDays(16))
        ));

//        when
        BookingDetails details = bookingService.updateBooking(newBookingRequest);

//        then
        assertThat(details.stays()).hasSize(2);
        assertThat(stayToBeRemoved.getStatus()).isEqualTo(RoomStayStatus.CANCELLED);
    }

    @ParameterizedTest
    @EnumSource(value = RoomStayStatus.class, names = {"ACTIVE", "COMPLETED", "NOSHOW"})
    public void should_fail_when_removing_stay_in_illegal_status(RoomStayStatus illegalStatus) {
//        given
        Customer customer = CustomerTestUtils.aValidCustomer().build();
        Employee employee = mockEmployee();
        Room room = RoomTestUtils.aValidRoom().build();

        Booking booking = BookingTestUtils.aValidBooking(customer, employee).id(1L).build();

        RoomStay stayToBeRemoved1 = buildStay(2L, room, employee, today.plusDays(10), today.plusDays(12));
        stayToBeRemoved1.setStatus(illegalStatus);
        booking.addStay(stayToBeRemoved1);

        mockBooking(booking);

        var newBookingRequest = createBookingUpdateRequest(1L, employeeId, List.of());

//        when and then
        assertThatThrownBy(() ->bookingService.updateBooking(newBookingRequest))
                .isInstanceOf(BookingValidationException.class)
                .satisfies(ex ->{
                    BookingValidationException roomStayEx = (BookingValidationException) ex;
                    assertThat(roomStayEx.getBadStatusDetails())
                            .extracting(RoomStayBadStatusDetails::id, RoomStayBadStatusDetails::status, RoomStayBadStatusDetails::errorCode)
                            .containsExactlyInAnyOrder(tuple(2L, illegalStatus, RoomStayErrorCode.ONLY_PLANNED_STAY_CAN_BE_CANCELLED));
                });
    }

    @Test
    public void should_change_active_stay_end_date() {
//        given
        Customer customer = CustomerTestUtils.aValidCustomer().build();
        Employee employee = mockEmployee();
        mockBooking();

        Room room = mockRoom(2L);


        Booking booking = BookingTestUtils.aValidBooking(customer, employee).id(1L).build();
        booking.addStay(buildStay(5L, room, employee, today.plusDays(10), today.plusDays(20)));
        booking.getStays().getFirst().setStatus(RoomStayStatus.ACTIVE);
        mockBooking(booking);

        var newBookingRequest = createBookingUpdateRequest(1L, employeeId, List.of(
                createRoomStayUpdateRequest(5L, 2L, today.plusDays(10), today.plusDays(22))
        ));

//        when
        BookingDetails details = bookingService.updateBooking(newBookingRequest);

//        then
        assertThat(details.stays())
                .extracting(RoomStayDetails::id, RoomStayDetails::status, RoomStayDetails::activeTo)
                .containsExactlyInAnyOrder(tuple(5L, RoomStayStatus.ACTIVE, today.plusDays(22)));
    }

    @Test
    public void should_not_change_active_stay_start_date() {
//        given
        Customer customer = CustomerTestUtils.aValidCustomer().build();
        Employee employee = mockEmployee();

        Room room = mockRoom(2L);


        Booking booking = BookingTestUtils.aValidBooking(customer, employee).id(1L).build();
        booking.addStay(buildStay(5L, room, employee, today.plusDays(10), today.plusDays(20)));
        booking.getStays().getFirst().setStatus(RoomStayStatus.ACTIVE);
        mockBooking(booking);

        var newBookingRequest = createBookingUpdateRequest(1L, employeeId, List.of(
                createRoomStayUpdateRequest(5L, 2L, today.plusDays(12), today.plusDays(20))
        ));

//        when and then
        assertThatThrownBy(() ->bookingService.updateBooking(newBookingRequest))
                .isInstanceOf(BookingValidationException.class)
                .satisfies(ex ->{
                    BookingValidationException bookingEx = (BookingValidationException) ex;
                    assertThat(bookingEx.getBadStatusDetails())
                            .extracting(RoomStayBadStatusDetails::id, RoomStayBadStatusDetails::status, RoomStayBadStatusDetails::errorCode)
                            .containsExactlyInAnyOrder(tuple(5L, RoomStayStatus.ACTIVE, RoomStayErrorCode.ONLY_PLANNED_STAY_CAN_HAVE_START_DATE_EDITED));

                });
    }

    private Employee mockEmployee() {
        Employee employee = EmployeeTestUtils
                .aValidEmployee(PersonTestUtils.aValidPerson().id(employeeId).build())
                .id(employeeId)
                .build();

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        return employee;
    }

    private void mockCustomer() {
        mockCustomer(BigDecimal.valueOf(0.13));
    }

    private void mockCustomer(BigDecimal discount) {

        Customer customer = CustomerTestUtils
                .aValidCustomer(PersonTestUtils.aValidPerson().id(customerId).build(),
                        CustomerTestUtils
                                .aValidLoyaltyStatus()
                                .discount(discount)
                                .build())
                .id(customerId)
                .build();

        when(customerRepository.findById(customerId))
                .thenReturn(Optional.of(customer));
    }

    private void mockBooking() {
        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> {
                    Booking bookingArg = invocation.getArgument(0);
                    bookingArg.setId(10L);
                    return bookingArg;
                });
    }

    private void mockBooking(Booking booking) {
        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));
    }

    private Room mockRoom(Long id) {
        Room room = RoomTestUtils.aValidRoom(RoomTestUtils.aValidType().build())
                                 .id(id)
                                 .build();
        when(roomRepository.findById(id)).thenReturn(Optional.of(room));
        return room;
    }

    private Room mockRoom(Long id, String name) {
        Room room = RoomTestUtils.aValidRoom(RoomTestUtils.aValidType().build())
                                 .id(id)
                                 .name(name)
                                 .build();
        when(roomRepository.findById(id)).thenReturn(Optional.of(room));
        return room;
    }

    private Room mockRoom(Long id, BigDecimal pricePerNight) {
        Room room = RoomTestUtils.aValidRoom(RoomTestUtils.aValidType().pricePerNight(pricePerNight).build())
                                 .id(id)
                                 .build();
        when(roomRepository.findById(id)).thenReturn(Optional.of(room));
        return room;
    }

    private void mockRoomStayConflicts(Long bookingId, Long roomId, LocalDate from, LocalDate to, RoomStay... conflicts) {
        when(roomStayRepository.getConflicts(
                roomId,
                List.of(RoomStayStatus.ACTIVE, RoomStayStatus.PLANNED),
                from,
                to,
                bookingId
        )).thenReturn(List.of(conflicts));
    }

    private RoomStay buildStay(Long id, Room room, Employee employee, LocalDate from, LocalDate to){
        return BookingTestUtils.aValidRoomStay(null, room,employee).id(id).activeFrom(from).activeTo(to).build();
    }
}