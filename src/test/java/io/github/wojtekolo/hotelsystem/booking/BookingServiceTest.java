package io.github.wojtekolo.hotelsystem.booking;

import io.github.wojtekolo.hotelsystem.common.person.Person;
import io.github.wojtekolo.hotelsystem.common.person.PersonRepository;
import io.github.wojtekolo.hotelsystem.common.person.PersonTestUtils;
import io.github.wojtekolo.hotelsystem.customer.*;
import io.github.wojtekolo.hotelsystem.employee.Employee;
import io.github.wojtekolo.hotelsystem.employee.EmployeeRepository;
import io.github.wojtekolo.hotelsystem.employee.EmployeeTestUtils;
import io.github.wojtekolo.hotelsystem.room.*;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Transactional
@TestPropertySource(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Disabled
class BookingServiceTest {
    @Autowired
    BookingService bookingService;

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    RoomTypeRepository roomTypeRepository;

    @Autowired
    PersonRepository personRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    LoyaltyStatusRepository loyaltyStatusRepository;

    @Autowired
    EmployeeRepository employeeRepository;

    private final LocalDate today = LocalDate.now();

    @Test
    public void should_persist_single_stay(){
//        given
        int days=5;
        BookingTestContext context = prepareContext();

        var singleStayRequest = new SingleRoomStayRequest(
                context.room().getId(),
                today.plusDays(5),
                today.plusDays(5+days),
                null
        );

        var bookingCreateRequest = new BookingCreateRequest(
                context.customer().getId(),
                context.employee().getId(),
                List.of(singleStayRequest)
        );

//        when
        BookingDetails result = bookingService.addBooking(bookingCreateRequest);

//        then
        Booking savedBooking = bookingRepository.findById(result.id())
                .orElseThrow(() -> new AssertionError("Booking not found in database"));

        assertThat(savedBooking.getId()).isNotNull();
        assertThat(savedBooking.getCustomer().getId()).isEqualTo(context.customer().getId());
        assertThat(savedBooking.getStays().getFirst().getRoom().getId()).isEqualTo(context.room().getId());
        assertThat(savedBooking.getPaymentStatus()).isEqualTo(PaymentStatus.UNPAID);
        assertThat(savedBooking.getStatus()).isEqualTo(BookingStatus.PLANNED);
        assertThat(savedBooking.getCreatedBy().getId()).isEqualTo(context.employee().getId());


        assertThat(result.customerName()).isEqualTo(context.customer().getPerson().getName());
        assertThat(result.stays()).hasSize(1);
        assertThat(result.stays().getFirst().roomName()).isEqualTo(context.room().getName());

        assertThat(result.status()).isEqualTo(BookingStatus.PLANNED);
        assertThat(result.paymentStatus()).isEqualTo(PaymentStatus.UNPAID);
    }

    @Test
    public void should_persist_multiple_stays(){
//        given
        int days=5;
        BookingTestContext context = prepareContext();

        Room room1 = context.room();
        Room room2 = roomRepository.save(RoomTestUtils.aValidRoom(room1.getType()).build());
        Room room3 = roomRepository.save(RoomTestUtils.aValidRoom(room1.getType()).build());

        var singleStayRequest1 = new SingleRoomStayRequest(
                room1.getId(),
                today.plusDays(5),
                today.plusDays(5+days),
                null
        );

        var singleStayRequest2 = new SingleRoomStayRequest(
                room2.getId(),
                today.plusDays(3),
                today.plusDays(3+days),
                null
        );

        var singleStayRequest3 = new SingleRoomStayRequest(
                room3.getId(),
                today.plusDays(2),
                today.plusDays(2+days),
                null
        );

        var bookingCreateRequest = new BookingCreateRequest(
                context.customer().getId(),
                context.employee().getId(),
                List.of(singleStayRequest1, singleStayRequest2, singleStayRequest3)
        );

//        when
        BookingDetails result = bookingService.addBooking(bookingCreateRequest);

//        then
        Booking savedBooking = bookingRepository.findById(result.id())
                .orElseThrow(() -> new AssertionError("Booking not found in database"));

        assertThat(savedBooking.getStays()).hasSize(3);
    }


    @Test
    public void should_calculate_correct_cost_when_custom_price_and_single_stay(){
//        given
        int days=5;
        BookingTestContext context = prepareContext();

        Customer customer = customerWithDiscount(BigDecimal.valueOf(0.1));
        Room room = roomWithPrice(BigDecimal.valueOf(1000));

        var singleStayRequest = new SingleRoomStayRequest(
                room.getId(),
                today.plusDays(5),
                today.plusDays(5+days),
                BigDecimal.valueOf(700)
        );

        var bookingCreateRequest = new BookingCreateRequest(
                customer.getId(),
                context.employee().getId(),
                List.of(singleStayRequest)
        );

//        when
        BookingDetails result = bookingService.addBooking(bookingCreateRequest);

        Booking savedBooking = bookingRepository.findById(result.id())
                .orElseThrow(() -> new AssertionError("Booking not found in database"));

//        Don't take discount from loyalty status into account when price per night is custom
        assertThat(savedBooking.getStays().getFirst().getPricePerNight()).isEqualByComparingTo(BigDecimal.valueOf(700));
        assertThat(result.totalCost()).isEqualByComparingTo(BigDecimal.valueOf(3500));

    }


    @Test
    public void should_calculate_correct_cost_when_custom_price_and_multiple_stays(){
//        given
        int days=5;
        BookingTestContext context = prepareContext();

        Customer customer = customerWithDiscount(BigDecimal.valueOf(0.1));
        Room room1 = roomWithPrice(BigDecimal.valueOf(100));
        Room room2 = roomWithPrice(BigDecimal.valueOf(200));
        Room room3 = roomWithPrice(BigDecimal.valueOf(300));

        var singleStayRequest1 = new SingleRoomStayRequest(
                room1.getId(),
                today.plusDays(5),
                today.plusDays(5+days),
                BigDecimal.valueOf(500)
        );
//        5 * 500 = 2500

        var singleStayRequest2 = new SingleRoomStayRequest(
                room2.getId(),
                today.plusDays(5),
                today.plusDays(6+days),
                BigDecimal.valueOf(600)
        );
//        6 * 600 = 3600

        var singleStayRequest3 = new SingleRoomStayRequest(
                room3.getId(),
                today.plusDays(5),
                today.plusDays(7+days),
                BigDecimal.valueOf(700)
        );
//        7 * 700 = 4900

//        total = 4900 + 3600 + 2500 = 11 000

        var bookingCreateRequest = new BookingCreateRequest(
                customer.getId(),
                context.employee().getId(),
                List.of(singleStayRequest1, singleStayRequest2, singleStayRequest3)
        );

//        when
        BookingDetails result = bookingService.addBooking(bookingCreateRequest);

        Booking savedBooking = bookingRepository.findById(result.id())
                .orElseThrow(() -> new AssertionError("Booking not found in database"));

//        Don't take discount from loyalty status into account when price per night is custom
        assertThat(result.totalCost()).isEqualByComparingTo(BigDecimal.valueOf(11000));

    }

    @Test
    public void should_calculate_correct_cost_when_default_price_and_single_stay(){
//        given
        int days=5;
        BookingTestContext context = prepareContext();

        Customer customer = customerWithDiscount(BigDecimal.valueOf(0.1));
        Room room = roomWithPrice(BigDecimal.valueOf(700));


        var singleStayRequest = new SingleRoomStayRequest(
                room.getId(),
                today.plusDays(5),
                today.plusDays(5+days),
                null
        );

        var bookingCreateRequest = new BookingCreateRequest(
                customer.getId(),
                context.employee().getId(),
                List.of(singleStayRequest)
        );

//        when
        BookingDetails result = bookingService.addBooking(bookingCreateRequest);

        Booking savedBooking = bookingRepository.findById(result.id())
                .orElseThrow(() -> new AssertionError("Booking not found in database"));

        assertThat(savedBooking.getStays().getFirst().getPricePerNight()).isEqualByComparingTo(BigDecimal.valueOf(630));
        assertThat(result.totalCost()).isEqualByComparingTo(BigDecimal.valueOf(3150));
    }


    @Test
    public void should_calculate_correct_cost_when_default_price_and_multiple_stays(){
//        given
        int days=5;
        BookingTestContext context = prepareContext();

        Customer customer = customerWithDiscount(BigDecimal.valueOf(0.1));
        Room room1 = roomWithPrice(BigDecimal.valueOf(100));
        Room room2 = roomWithPrice(BigDecimal.valueOf(200));
        Room room3 = roomWithPrice(BigDecimal.valueOf(300));

        var singleStayRequest1 = new SingleRoomStayRequest(
                room1.getId(),
                today.plusDays(5),
                today.plusDays(5+days),
                null
        );
//        5 * 100 = 500

        var singleStayRequest2 = new SingleRoomStayRequest(
                room2.getId(),
                today.plusDays(5),
                today.plusDays(5+days+1),
                null
        );
//        6 * 200 = 1200

        var singleStayRequest3 = new SingleRoomStayRequest(
                room3.getId(),
                today.plusDays(5),
                today.plusDays(5+days+2),
                null
        );
//        7 * 300 = 2100

//        total = 2100 + 1200 + 500 = 3800
//        after discount = 3800 * 0.9 = 3420

        var bookingCreateRequest = new BookingCreateRequest(
                customer.getId(),
                context.employee().getId(),
                List.of(singleStayRequest1, singleStayRequest2, singleStayRequest3)
        );

//        when
        BookingDetails result = bookingService.addBooking(bookingCreateRequest);

        Booking savedBooking = bookingRepository.findById(result.id())
                .orElseThrow(() -> new AssertionError("Booking not found in database"));

        assertThat(result.totalCost()).isEqualByComparingTo(BigDecimal.valueOf(3420));

    }

    private BookingTestContext prepareContext() {
        RoomType type = roomTypeRepository.save(RoomTestUtils.aValidType().build());
        Room room = roomRepository.save(RoomTestUtils.aValidRoom(type).build());

        Person personCustomer = personRepository.save(PersonTestUtils.aValidPerson().build());
        LoyaltyStatus loyaltyStatus = loyaltyStatusRepository.save(CustomerTestUtils.aValidLoyaltyStatus().build());
        Customer customer = customerRepository.save(CustomerTestUtils.aValidCustomer(personCustomer, loyaltyStatus).build());

        Person personEmp = personRepository.save(PersonTestUtils.aValidPerson().build());
        Employee employee = employeeRepository.save(EmployeeTestUtils.aValidEmployee(personEmp).build());

        return new BookingTestContext(customer, employee, room);
    }

    private Customer customerWithDiscount(BigDecimal discount){
        LoyaltyStatus loyaltyStatus = loyaltyStatusRepository.save(CustomerTestUtils.aValidLoyaltyStatus().discount(discount).build());
        Person person = personRepository.save(PersonTestUtils.aValidPerson().build());
        return customerRepository.save(CustomerTestUtils.aValidCustomer(person,loyaltyStatus).build());
    }

    private Room roomWithPrice(BigDecimal pricePerNight){
        RoomType roomType = roomTypeRepository.save(RoomTestUtils.aValidType().pricePerNight(pricePerNight).build());
        return roomRepository.save(RoomTestUtils.aValidRoom(roomType).build());
    }
}