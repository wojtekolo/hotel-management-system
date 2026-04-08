package io.github.wojtekolo.hotelsystem.booking;

import io.github.wojtekolo.hotelsystem.booking.api.*;
import io.github.wojtekolo.hotelsystem.booking.model.*;
import io.github.wojtekolo.hotelsystem.customer.model.Customer;
import io.github.wojtekolo.hotelsystem.customer.CustomerTestUtils;
import io.github.wojtekolo.hotelsystem.employee.model.Employee;
import io.github.wojtekolo.hotelsystem.employee.EmployeeTestUtils;
import io.github.wojtekolo.hotelsystem.room.model.Room;
import io.github.wojtekolo.hotelsystem.room.RoomTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BookingTestUtils {

    public static final Long DEFAULT_EMPLOYEE_ID = 1L;
    public static final Long DEFAULT_CUSTOMER_ID = 20L;

    public static Booking.BookingBuilder aValidBooking(Customer customer, Employee createEmployee) {
        return Booking.builder()
                .customer(customer)
                .createBy(createEmployee)
                .status(BookingStatus.PLANNED)
                .paymentStatus(PaymentStatus.UNPAID)
                .stays(new ArrayList<>());
    }

    public static Booking.BookingBuilder aValidBooking() {
        return Booking.builder()
                .customer(CustomerTestUtils.aValidCustomer().build())
                .createBy(EmployeeTestUtils.aValidEmployee().build())
                .status(BookingStatus.PLANNED)
                .paymentStatus(PaymentStatus.UNPAID)
                .stays(new ArrayList<>());
    }

    public static BookingDetails aValidBookingDetails() {
        return new BookingDetails(
                1L,
                "CustomerFullName",
                "CustomerPhone",
                "0",
                LocalDateTime.now().minusDays(5),
                BigDecimal.TEN,
                "createBy",
                PaymentStatus.UNPAID,
                BookingStatus.PLANNED,
                List.of(
                        new RoomStayDetails(
                                1L,
                                1L,
                                "roomName",
                                "roomType",
                                null,
                                null,
                                LocalDate.now().plusDays(5),
                                LocalDate.now().plusDays(10),
                                BigDecimal.TEN,
                                BigDecimal.valueOf(50),
                                RoomStayStatus.PLANNED,
                                "createBy",
                                null,
                                null
                        )
                )
        );
    }

    public static RoomStay.RoomStayBuilder aValidRoomStay(Booking booking, Room room, Employee createEmployee) {
        return RoomStay.builder()
                .booking(booking)
                .room(room)
                .createBy(createEmployee)
                .pricePerNight(BigDecimal.valueOf(500))
                .activeFrom(LocalDate.now().plusDays(10))
                .activeTo(LocalDate.now().plusDays(15))
                .status(RoomStayStatus.PLANNED);
    }

    public static RoomStay.RoomStayBuilder aValidRoomStay() {
        return RoomStay.builder()
                .booking(aValidBooking().build())
                .room(RoomTestUtils.aValidRoom().build())
                .createBy(EmployeeTestUtils.aValidEmployee().build())
                .pricePerNight(BigDecimal.valueOf(500))
                .activeFrom(LocalDate.now().plusDays(10))
                .activeTo(LocalDate.now().plusDays(15))
                .status(RoomStayStatus.PLANNED);
    }

    public static RoomStay buildRoomStay(Long id, Room room, LocalDate from, LocalDate to) {
        return aValidRoomStay()
                .id(id)
                .room(room)
                .activeFrom(from)
                .activeTo(to)
                .build();
    }

    public static BookingCreateRequest createBookingCreateRequest(List<RoomStayCreateRequest> stayRequests) {
        return new BookingCreateRequest(
                DEFAULT_CUSTOMER_ID,
                DEFAULT_EMPLOYEE_ID,
                stayRequests
        );
    }

    public static BookingUpdateRequest createBookingUpdateRequest(Long employeeId, List<RoomStayUpdateRequest> stayRequests) {
        return new BookingUpdateRequest(
                employeeId,
                stayRequests
        );
    }

    public static RoomStayCreateRequest createRoomStayCreateRequest(Long roomId, LocalDate from, LocalDate to) {
        return createRoomStayCreateRequest(
                roomId,
                from,
                to,
                null
        );
    }

    public static RoomStayCreateRequest createRoomStayCreateRequest(Long roomId, LocalDate from, LocalDate to, BigDecimal pricePerNight) {
        return new RoomStayCreateRequest(
                roomId,
                from,
                to,
                pricePerNight
        );
    }

    public static RoomStayUpdateRequest createRoomStayUpdateRequest(Long id, Long roomId, LocalDate from, LocalDate to) {
        return new RoomStayUpdateRequest(
                id,
                roomId,
                from,
                to,
                null
        );
    }
}
