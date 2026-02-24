package io.github.wojtekolo.hotelsystem.booking;

import io.github.wojtekolo.hotelsystem.customer.Customer;
import io.github.wojtekolo.hotelsystem.employee.Employee;
import io.github.wojtekolo.hotelsystem.room.Room;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookingTestUtils {

    public static final Long DEFAULT_EMPLOYEE_ID = 1L;
    public static final Long DEFAULT_CUSTOMER_ID = 20L;

    public static Booking.BookingBuilder aValidBooking(Customer customer, Employee createEmployee){
        return Booking.builder()
                .customer(customer)
                .createBy(createEmployee)
                .status(BookingStatus.PLANNED)
                .paymentStatus(PaymentStatus.UNPAID)
                .stays(new ArrayList<>());
    }

    public static RoomStay.RoomStayBuilder aValidRoomStay(Booking booking, Room room, Employee createEmployee){
        return RoomStay.builder()
                .booking(booking)
                .room(room)
                .createBy(createEmployee)
                .pricePerNight(BigDecimal.valueOf(500))
                .activeFrom(LocalDate.now().plusDays(10))
                .activeTo(LocalDate.now().plusDays(15))
                .status(RoomStayStatus.PLANNED);
    }

    public static RoomStayCreateRequest.RoomStayCreateRequestBuilder aValidRoomStayRequest(Long roomId, LocalDate today){
        return RoomStayCreateRequest.builder()
                .roomId(roomId)
                .from(today.plusDays(1))
                .to(today.plusDays(10))
                .customPricePerNight(BigDecimal.valueOf(500));
    }

    public static BookingCreateRequest createBookingCreateRequest(List<RoomStayCreateRequest> stayRequests) {
        return new BookingCreateRequest(
                DEFAULT_CUSTOMER_ID,
                DEFAULT_EMPLOYEE_ID,
                stayRequests
        );
    }

    public static BookingCreateRequest createBookingCreateRequest(Long employeeId, Long customerId, List<RoomStayCreateRequest> stayRequests) {
        return new BookingCreateRequest(
                customerId,
                employeeId,
                stayRequests
        );
    }

    public static BookingUpdateRequest createBookingUpdateRequest(Long bookingId, Long employeeId, List<RoomStayUpdateRequest> stayRequests) {
        return new BookingUpdateRequest(
                bookingId,
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

//    public static RoomStayUpdateRequest createRoomStayUpdateRequest(Long id, Long roomId, LocalDate from, LocalDate to, BigDecimal pricePerNight) {
//        return createRoomStayUpdateRequest(
//                id,
//                roomId,
//                from,
//                to,
//                pricePerNight
//        );
//    }
}
