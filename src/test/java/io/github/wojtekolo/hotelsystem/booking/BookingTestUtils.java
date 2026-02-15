package io.github.wojtekolo.hotelsystem.booking;

import io.github.wojtekolo.hotelsystem.customer.Customer;
import io.github.wojtekolo.hotelsystem.employee.Employee;
import io.github.wojtekolo.hotelsystem.room.Room;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BookingTestUtils {

    public static Booking.BookingBuilder aValidBooking(Customer customer, Employee createEmployee){
        return Booking.builder()
                .customer(customer)
                .createdBy(createEmployee)
                .status(BookingStatus.PLANNED)
                .paymentStatus(PaymentStatus.UNPAID);
    }

    public static RoomStay.RoomStayBuilder aValidRoomStay(Booking booking, Room room, Employee createEmployee){
        return RoomStay.builder()
                .booking(booking)
                .room(room)
                .createdBy(createEmployee)
                .pricePerNight(BigDecimal.valueOf(500))
                .activeFrom(LocalDate.now().plusDays(10))
                .activeTo(LocalDate.now().plusDays(15))
                .status(RoomStayStatus.PLANNED);
    }
}
