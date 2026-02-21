package io.github.wojtekolo.hotelsystem.booking;

import io.github.wojtekolo.hotelsystem.common.exceptions.BookingConflictException;
import io.github.wojtekolo.hotelsystem.common.exceptions.BookingRequestConflictException;
import io.github.wojtekolo.hotelsystem.common.exceptions.ResourceNotFoundException;
import io.github.wojtekolo.hotelsystem.common.person.PersonTestUtils;
import io.github.wojtekolo.hotelsystem.customer.*;
import io.github.wojtekolo.hotelsystem.employee.Employee;
import io.github.wojtekolo.hotelsystem.employee.EmployeeMapper;
import io.github.wojtekolo.hotelsystem.employee.EmployeeRepository;
import io.github.wojtekolo.hotelsystem.employee.EmployeeTestUtils;
import io.github.wojtekolo.hotelsystem.room.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private CustomerMapper customerMapper;

    @Mock
    private EmployeeMapper employeeMapper;


    @Mock
    EmployeeRepository employeeRepository;

    @Mock
    RoomRepository roomRepository;

    @Mock
    CustomerRepository customerRepository;

    @Mock
    BookingRepository bookingRepository;

    @Mock
    RoomStayRepository roomStayRepository;

    @InjectMocks
    BookingService bookingService;

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
                roomStayRepository,
                bookingRepository
        );
    }

    @Test
    public void should_calculate_correct_cost_when_custom_price_and_single_stay() {
//        given
        mockEmployee();
        mockCustomer(BigDecimal.valueOf(0.03));
        mockBooking();
        mockRoom(1L, BigDecimal.valueOf(500));

        var request = createBookingCreateRequest(List.of(
                createRoomStayCreateRequestWithPrice(
                        1L,
                        today.plusDays(5),
                        today.plusDays(10),
                        BigDecimal.valueOf(700))
        ));

//        when
        BookingDetails result = bookingService.addBooking(request);

//        then
//        Don't take discount from loyalty status into account when price per night is custom
        assertThat(result.stays()).isNotNull();
        assertThat(result.stays()).hasSize(1);
        assertThat(result.stays().getFirst().pricePerNight()).isEqualByComparingTo(BigDecimal.valueOf(700));
        assertThat(result.totalCost()).isEqualByComparingTo(BigDecimal.valueOf(3500));
    }


    @Test
    public void should_calculate_correct_cost_when_custom_price_and_multiple_stays() {
//        given
        mockEmployee();
        mockCustomer(BigDecimal.valueOf(0.03));
        mockBooking();

        Room room1 = mockRoom(1L, BigDecimal.valueOf(100));
        Room room2 = mockRoom(2L, BigDecimal.valueOf(200));
        Room room3 = mockRoom(3L, BigDecimal.valueOf(300));

        BookingCreateRequest request = createBookingCreateRequest(List.of(
//                      5 * 500 = 2500
                        createRoomStayCreateRequestWithPrice(
                                room1.getId(),
                                today.plusDays(5),
                                today.plusDays(10),
                                BigDecimal.valueOf(500)
                        ),

//                      6 * 600 = 3600
                        createRoomStayCreateRequestWithPrice(
                                room2.getId(),
                                today.plusDays(5),
                                today.plusDays(11),
                                BigDecimal.valueOf(600)
                        ),

//                      7 * 700 = 4900
                        createRoomStayCreateRequestWithPrice(
                                room3.getId(),
                                today.plusDays(5),
                                today.plusDays(12),
                                BigDecimal.valueOf(700)
                        )
                )
        );
//        total = 4900 + 3600 + 2500 = 11 000

//        when
        BookingDetails result = bookingService.addBooking(request);


//        then
        Map<Long, BigDecimal> staysPricePerNight = mapStaysPrices(result.stays());

        assertThat(staysPricePerNight.get(room1.getId())).isEqualByComparingTo(BigDecimal.valueOf(500));
        assertThat(staysPricePerNight.get(room2.getId())).isEqualByComparingTo(BigDecimal.valueOf(600));
        assertThat(staysPricePerNight.get(room3.getId())).isEqualByComparingTo(BigDecimal.valueOf(700));

//        Don't take discount from loyalty status into account when price per night is custom
        assertThat(result.totalCost()).isEqualByComparingTo(BigDecimal.valueOf(11000));

    }

    @Test
    public void should_calculate_correct_cost_when_default_price_and_single_stay() {
//        given
        mockEmployee();
        mockCustomer(BigDecimal.valueOf(0.1));
        mockBooking();
        Room room = mockRoom(1L, BigDecimal.valueOf(700));

        BookingCreateRequest request = createBookingCreateRequest(List.of(
                createRoomStayCreateRequest(room.getId(), today.plusDays(5), today.plusDays(10))
        ));

//        when
        BookingDetails result = bookingService.addBooking(request);

//        then
        assertThat(result.stays()).isNotNull();
        assertThat(result.stays()).hasSize(1);
        assertThat(result.stays().getFirst().pricePerNight()).isEqualByComparingTo(BigDecimal.valueOf(630));
        assertThat(result.totalCost()).isEqualByComparingTo(BigDecimal.valueOf(3150));
    }

    //
//
    @Test
    public void should_calculate_correct_cost_when_default_price_and_multiple_stays() {
//        given
        mockEmployee();
        mockCustomer(BigDecimal.valueOf(0.1));
        mockBooking();

        Room room1 = mockRoom(1L, BigDecimal.valueOf(100));
        Room room2 = mockRoom(2L, BigDecimal.valueOf(200));
        Room room3 = mockRoom(3L, BigDecimal.valueOf(300));

        BookingCreateRequest request = createBookingCreateRequest(List.of(
//              5 * 100 = 500
                createRoomStayCreateRequest(room1.getId(), today.plusDays(5), today.plusDays(10)),

//              6 * 200 = 1200
                createRoomStayCreateRequest(room2.getId(), today.plusDays(5), today.plusDays(11)),

//              7 * 300 = 2100
                createRoomStayCreateRequest(room3.getId(), today.plusDays(5), today.plusDays(12))
        ));

//        default total = 2100 + 1200 + 500 = 3800
//        after loyalty status loyaltyDiscount = 3800 * 0.9 = 3420

//        when
        BookingDetails result = bookingService.addBooking(request);

//        then
        Map<Long, BigDecimal> staysPricePerNight = mapStaysPrices(result.stays());

        assertThat(staysPricePerNight.get(room1.getId())).isEqualByComparingTo(BigDecimal.valueOf(90));
        assertThat(staysPricePerNight.get(room2.getId())).isEqualByComparingTo(BigDecimal.valueOf(180));
        assertThat(staysPricePerNight.get(room3.getId())).isEqualByComparingTo(BigDecimal.valueOf(270));

        assertThat(result.totalCost()).isEqualByComparingTo(BigDecimal.valueOf(3420));
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

    @Test
    public void should_throw_booking_conflict_when_single_stay() {
//        given
        mockEmployee();
        mockCustomer();

        Room room = mockRoom(1L, "roomname");

        LocalDate from = today.plusDays(5);
        LocalDate to = today.plusDays(10);

        BookingCreateRequest request = createBookingCreateRequest(List.of(
                createRoomStayCreateRequest(1L, today.plusDays(5), today.plusDays(10)
                )
        ));

        mockRoomStayConflicts(1L, from, to,
                RoomStay.builder().id(15L).room(room).activeFrom(from.plusDays(1)).activeTo(to).build());

//        when
        assertThatThrownBy(() ->
                bookingService.addBooking(request)
        )
                .isInstanceOf(BookingConflictException.class)
                .satisfies(ex -> {
                    BookingConflictException bookingEx = (BookingConflictException) ex;

                    assertThat(bookingEx.getConflicts())
                            .extracting(RoomStayConflict::roomName)
                            .contains("roomname");

                    assertThat(bookingEx.getConflicts())
                            .flatExtracting(RoomStayConflict::roomConflictsDetails)
                            .extracting(RoomStayConflictDetails::roomStayId)
                            .contains(15L);
                });
    }

    @Test
    public void booking_conflict_response_should_contain_only_rooms_unavailable_in_chosen_period() {
//        given
        mockEmployee();
        mockCustomer();

        Room room1 = mockRoom(1L, "roomname1");
        Room room2 = mockRoom(2L, "roomname2");
        Room room3 = mockRoom(3L, "roomname3");

        LocalDate from = today.plusDays(5);
        LocalDate to = today.plusDays(10);

        BookingCreateRequest request = createBookingCreateRequest(List.of(
                createRoomStayCreateRequest(room1.getId(), from, to),
                createRoomStayCreateRequest(room2.getId(), from, to),
                createRoomStayCreateRequest(room3.getId(), from, to)
        ));

        mockRoomStayConflicts(1L, from, to,
                RoomStay.builder().id(15L).room(room1).activeFrom(from.plusDays(1)).activeTo(to).build()
        );

        mockRoomStayConflicts(room2.getId(), from, to,
                RoomStay.builder().id(16L).room(room2).activeFrom(from).activeTo(to.plusDays(1)).build(),
                RoomStay.builder().id(17L).room(room2).activeFrom(from).activeTo(to).build()
        );

        mockRoomStayConflicts(room3.getId(), from, to);

        when(roomStayRepository.getConflicts(
                room3.getId(),
                List.of(RoomStayStatus.ACTIVE, RoomStayStatus.PLANNED),
                from,
                to
        )).thenReturn(
                List.of()
        );

//        when and then
        assertThatThrownBy(
                () -> bookingService.addBooking(request))
                .isInstanceOf(BookingConflictException.class)
                .isInstanceOf(BookingConflictException.class)
                .satisfies(e -> {
                    BookingConflictException bookingEx = (BookingConflictException) e;

                    assertThat(bookingEx.getConflicts())
                            .hasSize(2)
                            .extracting(RoomStayConflict::roomName)
                            .containsExactlyInAnyOrder("roomname1", "roomname2")
                            .doesNotContain("roomname3");

                    assertThat(bookingEx.getConflicts())
                            .flatExtracting(RoomStayConflict::roomConflictsDetails)
                            .extracting(RoomStayConflictDetails::roomStayId)
                            .containsExactlyInAnyOrder(15L, 16L, 17L);
                });


    }

    @Test
    public void should_throw_booking_request_conflict_when_there_is_overlap_between_sequential_stays() {
//        given
        mockEmployee();
        mockCustomer();
        mockRoom(1L);

        var booking = createBookingCreateRequest(List.of(
                createRoomStayCreateRequest(1L, today.plusDays(10), today.plusDays(15)),
                createRoomStayCreateRequest(1L, today.plusDays(13), today.plusDays(18)),
                createRoomStayCreateRequest(1L, today.plusDays(17), today.plusDays(20))
        ));

//        when and then
        assertThatThrownBy(() -> bookingService.addBooking(booking))
                .isInstanceOf(BookingRequestConflictException.class)
                .satisfies(e -> {
                    BookingRequestConflictException bookingEx = (BookingRequestConflictException) e;

                    assertThat(bookingEx.getConflicts())
                            .hasSize(2)
                            .extracting(InternalRoomStayConflict::from1)
                            .containsExactlyInAnyOrder(today.plusDays(10), today.plusDays(13));

                    assertThat(bookingEx.getConflicts())
                            .extracting(InternalRoomStayConflict::to2)
                            .containsExactlyInAnyOrder(today.plusDays(18), today.plusDays(20));
                });

    }

    @Test
    public void should_throw_booking_request_conflict_when_one_stay_overlaps_multiple_others() {
//        given
        mockEmployee();
        mockCustomer();
        mockRoom(1L);

        var request = createBookingCreateRequest(List.of(
                createRoomStayCreateRequest(1L, today.plusDays(10), today.plusDays(30)),
                createRoomStayCreateRequest(1L, today.plusDays(13), today.plusDays(15)),
                createRoomStayCreateRequest(1L, today.plusDays(16), today.plusDays(20)),
                createRoomStayCreateRequest(1L, today.plusDays(22), today.plusDays(25))
        ));

//        when and then
        assertThatThrownBy(() -> bookingService.addBooking(request))
                .isInstanceOf(BookingRequestConflictException.class)
                .satisfies(e -> {
                    BookingRequestConflictException bookingEx = (BookingRequestConflictException) e;

                    assertThat(bookingEx.getConflicts())
                            .hasSize(3)
                            .extracting(InternalRoomStayConflict::from1)
                            .containsExactlyInAnyOrder(today.plusDays(10), today.plusDays(10), today.plusDays(10));

                    assertThat(bookingEx.getConflicts())
                            .extracting(InternalRoomStayConflict::to2)
                            .containsExactlyInAnyOrder(today.plusDays(15), today.plusDays(20), today.plusDays(25));
                });

    }


    private Map<Long, BigDecimal> mapStaysPrices(List<RoomStayDetails> stays) {
        Map<Long, BigDecimal> roomsPricePerNight = new HashMap<>();

        for (RoomStayDetails stay : stays) {
            roomsPricePerNight.put(stay.roomId(), stay.pricePerNight());
        }
        return roomsPricePerNight;
    }


    private Employee mockEmployee() {
        Employee employee = EmployeeTestUtils
                .aValidEmployeeWithPerson(PersonTestUtils.aValidPerson().id(employeeId).build())
                .id(employeeId)
                .build();

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        return employee;
    }

    private Customer mockCustomer() {
        return mockCustomer(BigDecimal.valueOf(0.13));
    }

    private Customer mockCustomer(BigDecimal discount) {

        Customer customer = CustomerTestUtils
                .aValidCustomerWithPersonWithLoyalty(PersonTestUtils.aValidPerson().id(customerId).build(),
                        CustomerTestUtils
                                .aValidLoyaltyStatus()
                                .discount(discount)
                                .build())
                .id(customerId)
                .build();

        when(customerRepository.findById(customerId))
                .thenReturn(Optional.of(customer));
        return customer;
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

    private void setUpBookingSave(Booking booking) {
        when(bookingRepository.save(booking))
                .thenReturn(booking);
    }

    private Room mockRoom(Long id) {
        Room room = RoomTestUtils.aValidRoomWithType(RoomTestUtils.aValidType().build())
                                 .id(id)
                                 .build();
        when(roomRepository.findById(id)).thenReturn(Optional.of(room));
        return room;
    }

    private Room mockRoom(Long id, String name) {
        Room room = RoomTestUtils.aValidRoomWithType(RoomTestUtils.aValidType().build())
                                 .id(id)
                                 .name(name)
                                 .build();
        when(roomRepository.findById(id)).thenReturn(Optional.of(room));
        return room;
    }

    private Room mockRoom(Long id, BigDecimal pricePerNight) {
        Room room = RoomTestUtils.aValidRoomWithType(RoomTestUtils.aValidType().pricePerNight(pricePerNight).build())
                                 .id(id)
                                 .build();
        when(roomRepository.findById(id)).thenReturn(Optional.of(room));
        return room;
    }

    private BookingCreateRequest createBookingCreateRequest(List<RoomStayCreateRequest> stayRequests) {
        return new BookingCreateRequest(
                customerId,
                employeeId,
                stayRequests
        );
    }

    private BookingUpdateRequest createBookingUpdateRequest(Long bookingId, List<RoomStayUpdateRequest> stayRequests) {
        return new BookingUpdateRequest(
                bookingId,
                stayRequests
        );
    }

    private RoomStayCreateRequest createRoomStayCreateRequest(Long roomId, LocalDate from, LocalDate to) {
        return new RoomStayCreateRequest(
                roomId,
                from,
                to,
                null
        );
    }

    private RoomStayUpdateRequest createRoomStayUpdateRequest(Long id, Long roomId, LocalDate from, LocalDate to) {
        return new RoomStayUpdateRequest(
                id,
                roomId,
                from,
                to,
                null
        );
    }

    private RoomStayCreateRequest createRoomStayCreateRequestWithPrice(Long roomId, LocalDate from, LocalDate to, BigDecimal pricePerNight) {
        return new RoomStayCreateRequest(
                roomId,
                from,
                to,
                pricePerNight
        );
    }

    private RoomStayUpdateRequest createRoomStayUpdateRequestWithPricef(Long id, Long roomId, LocalDate from, LocalDate to, BigDecimal pricePerNight) {
        return new RoomStayUpdateRequest(
                id,
                roomId,
                from,
                to,
                pricePerNight
        );
    }

    private void mockRoomStayConflicts(Long roomId, LocalDate from, LocalDate to, RoomStay... conflicts) {
        when(roomStayRepository.getConflicts(
                roomId,
                List.of(RoomStayStatus.ACTIVE, RoomStayStatus.PLANNED),
                from,
                to
        )).thenReturn(List.of(conflicts));
    }
}