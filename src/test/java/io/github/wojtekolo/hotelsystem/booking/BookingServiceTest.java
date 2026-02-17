package io.github.wojtekolo.hotelsystem.booking;

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
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
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
        Employee employee = EmployeeTestUtils
                .aValidEmployee(PersonTestUtils.aValidPerson().id(employeeId).build())
                .id(employeeId)
                .build();

        lenient()
                .when(employeeRepository.findById(employeeId))
                .thenReturn(Optional.of(employee));

        lenient().
        when(customerRepository.findById(customerId))
                .thenReturn(Optional.of(customerWithDiscount(BigDecimal.valueOf(0.3))));

        lenient().
        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> {
                    Booking bookingArg = invocation.getArgument(0);
                    bookingArg.setId(10L);
                    return bookingArg;
                });

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
        Long roomId = 1L;

        var stayRequest = new SingleRoomStayRequest(
                roomId,
                today.plusDays(5),
                today.plusDays(10),
                BigDecimal.valueOf(700)
        );

        var request = new BookingCreateRequest(
                customerId,
                employeeId,
                List.of(stayRequest)
        );

        Room room = roomWithPrice(BigDecimal.valueOf(500), roomId);
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

//        when
        BookingDetails result = bookingService.addBooking(request);

//        then
//        Don't take loyaltyDiscount from loyalty status into account when price per night is custom
        assertThat(result.stays()).isNotNull();
        assertThat(result.stays()).hasSize(1);
        assertThat(result.stays().getFirst().pricePerNight()).isEqualByComparingTo(BigDecimal.valueOf(700));
        assertThat(result.totalCost()).isEqualByComparingTo(BigDecimal.valueOf(3500));
    }


    @Test
    public void should_calculate_correct_cost_when_custom_price_and_multiple_stays() {
//        given
        int days = 5;

        Customer customer = customerWithDiscount(BigDecimal.valueOf(0.1));
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        Room room1 = roomWithPrice(BigDecimal.valueOf(100), 1L);
        Room room2 = roomWithPrice(BigDecimal.valueOf(200), 2L);
        Room room3 = roomWithPrice(BigDecimal.valueOf(300), 3L);

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room1));
        when(roomRepository.findById(2L)).thenReturn(Optional.of(room2));
        when(roomRepository.findById(3L)).thenReturn(Optional.of(room3));


        var singleStayRequest1 = new SingleRoomStayRequest(
                room1.getId(),
                today.plusDays(5),
                today.plusDays(5 + days),
                BigDecimal.valueOf(500)
        );
//        5 * 500 = 2500

        var singleStayRequest2 = new SingleRoomStayRequest(
                room2.getId(),
                today.plusDays(5),
                today.plusDays(6 + days),
                BigDecimal.valueOf(600)
        );
//        6 * 600 = 3600

        var singleStayRequest3 = new SingleRoomStayRequest(
                room3.getId(),
                today.plusDays(5),
                today.plusDays(7 + days),
                BigDecimal.valueOf(700)
        );
//        7 * 700 = 4900
//        total = 4900 + 3600 + 2500 = 11 000

        var bookingCreateRequest = new BookingCreateRequest(
                customerId,
                employeeId,
                List.of(singleStayRequest1, singleStayRequest2, singleStayRequest3)
        );

//        when
        BookingDetails result = bookingService.addBooking(bookingCreateRequest);


//        then
        Map<Long, BigDecimal> staysPricePerNight = mapStaysPrices(result.stays());

        assertThat(staysPricePerNight.get(room1.getId())).isEqualByComparingTo(BigDecimal.valueOf(500));
        assertThat(staysPricePerNight.get(room2.getId())).isEqualByComparingTo(BigDecimal.valueOf(600));
        assertThat(staysPricePerNight.get(room3.getId())).isEqualByComparingTo(BigDecimal.valueOf(700));

//        Don't take loyaltyDiscount from loyalty status into account when price per night is custom
        assertThat(result.totalCost()).isEqualByComparingTo(BigDecimal.valueOf(11000));

    }

    @Test
    public void should_calculate_correct_cost_when_default_price_and_single_stay() {
//        given
        int days = 5;

        Customer customer = customerWithDiscount(BigDecimal.valueOf(0.1));
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        Room room = roomWithPrice(BigDecimal.valueOf(700), 1L);
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));


        var singleStayRequest = new SingleRoomStayRequest(
                room.getId(),
                today.plusDays(5),
                today.plusDays(5 + days),
                null
        );

        var bookingCreateRequest = new BookingCreateRequest(
                customerId,
                employeeId,
                List.of(singleStayRequest)
        );

//        when
        BookingDetails result = bookingService.addBooking(bookingCreateRequest);

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
        int days = 5;

        Customer customer = customerWithDiscount(BigDecimal.valueOf(0.1));
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        Room room1 = roomWithPrice(BigDecimal.valueOf(100), 1L);
        Room room2 = roomWithPrice(BigDecimal.valueOf(200), 2L);
        Room room3 = roomWithPrice(BigDecimal.valueOf(300), 3L);

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room1));
        when(roomRepository.findById(2L)).thenReturn(Optional.of(room2));
        when(roomRepository.findById(3L)).thenReturn(Optional.of(room3));


        var singleStayRequest1 = new SingleRoomStayRequest(
                room1.getId(),
                today.plusDays(5),
                today.plusDays(5 + days),
                null
        );
//        5 * 100 = 500

        var singleStayRequest2 = new SingleRoomStayRequest(
                room2.getId(),
                today.plusDays(5),
                today.plusDays(5 + days + 1),
                null
        );
//        6 * 200 = 1200

        var singleStayRequest3 = new SingleRoomStayRequest(
                room3.getId(),
                today.plusDays(5),
                today.plusDays(5 + days + 2),
                null
        );
//        7 * 300 = 2100

//        default total = 2100 + 1200 + 500 = 3800
//        after loyalty status loyaltyDiscount = 3800 * 0.9 = 3420

        var bookingCreateRequest = new BookingCreateRequest(
                customerId,
                employeeId,
                List.of(singleStayRequest1, singleStayRequest2, singleStayRequest3)
        );

//        when
        BookingDetails result = bookingService.addBooking(bookingCreateRequest);

//        then
        Map<Long, BigDecimal> staysPricePerNight = mapStaysPrices(result.stays());

        assertThat(staysPricePerNight.get(room1.getId())).isEqualByComparingTo(BigDecimal.valueOf(90));
        assertThat(staysPricePerNight.get(room2.getId())).isEqualByComparingTo(BigDecimal.valueOf(180));
        assertThat(staysPricePerNight.get(room3.getId())).isEqualByComparingTo(BigDecimal.valueOf(270));

        assertThat(result.totalCost()).isEqualByComparingTo(BigDecimal.valueOf(3420));
    }

    @Test
    public void should_throw_resource_not_found_when_invalid_employee(){
        var stayRequest = new SingleRoomStayRequest(
                1L,
                today.plusDays(5),
                today.plusDays(10),
                BigDecimal.valueOf(700)
        );

        var request = new BookingCreateRequest(
                customerId,
                employeeId+1,
                List.of(stayRequest)
        );
        assertThatThrownBy(() ->
                bookingService.addBooking(request)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Employee")
                .hasMessageContaining(String.valueOf(employeeId+1));
    }

    @Test
    public void should_throw_resource_not_found_when_invalid_customer(){
        var stayRequest = new SingleRoomStayRequest(
                1L,
                today.plusDays(5),
                today.plusDays(10),
                BigDecimal.valueOf(700)
        );

        var request = new BookingCreateRequest(
                customerId+1,
                employeeId,
                List.of(stayRequest)
        );
        assertThatThrownBy(() ->
                bookingService.addBooking(request)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Customer")
                .hasMessageContaining(String.valueOf(customerId+1));
    }

    @Test
    public void should_throw_resource_not_found_when_invalid_room(){
        var stayRequest = new SingleRoomStayRequest(
                15L,
                today.plusDays(5),
                today.plusDays(10),
                BigDecimal.valueOf(700)
        );

        var request = new BookingCreateRequest(
                customerId,
                employeeId,
                List.of(stayRequest)
        );
        assertThatThrownBy(() ->
                bookingService.addBooking(request)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Room")
                .hasMessageContaining(String.valueOf(15L));
    }

    private Customer customerWithDiscount(BigDecimal discount) {
        return CustomerTestUtils
                .aValidCustomer(PersonTestUtils.aValidPerson().id(customerId).build(),
                        CustomerTestUtils
                                .aValidLoyaltyStatus()
                                .discount(discount)
                                .build())
                .id(customerId)
                .build();
    }

    private Room roomWithPrice(BigDecimal pricePerNight, Long id) {
        return RoomTestUtils
                .aValidRoom(RoomTestUtils.aValidType().pricePerNight(pricePerNight).build())
                .id(id)
                .build();
    }

    private Map<Long, BigDecimal> mapStaysPrices(List<RoomStayDetails> stays) {
        Map<Long, BigDecimal> roomsPricePerNight = new HashMap<>();

        for (int i = 0; i < stays.size(); i++) {
            roomsPricePerNight.put(stays.get(i).roomId(), stays.get(i).pricePerNight());
        }
        return roomsPricePerNight;
    }
}