package io.github.wojtekolo.hotelsystem.booking;

import io.github.wojtekolo.hotelsystem.customer.CustomerMapper;
import io.github.wojtekolo.hotelsystem.employee.EmployeeMapper;
import org.mapstruct.*;

import java.math.BigDecimal;

@Mapper(
        componentModel = "spring",
        uses = {CustomerMapper.class, EmployeeMapper.class},
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface BookingMapper {

    @Mapping(target = "totalCost", source = "booking", qualifiedByName = "calculateBookingCost")
    @Mapping(target = "stays", source = "stays")
    @Mapping(target = "customerFullName", source = "customer")
    @Mapping(target = "customerPhone", source = "customer.privatePhone")
    @Mapping(target = "loyaltyDiscount", source = "customer.loyaltyStatus.discount")
    BookingDetails toBookingDetails(Booking booking);

    @Mapping(target = "roomId", source = "room.id")
    @Mapping(target = "roomName", source = "room.name")
    @Mapping(target = "roomType", source = "room.type.name")
    @Mapping(target = "totalCost", source = "roomStay", qualifiedByName = "calculateRoomStayCost")
    RoomStayDetails toRoomStayDetails(RoomStay roomStay);

    @Mapping(target = "bookingId", source = "booking.id")
    @Mapping(target = "roomStayId", source = "id")
    @Mapping(target = "from", source = "activeFrom")
    @Mapping(target = "to", source = "activeTo")
    RoomStayConflictDetails toRoomStayConflictDetails(RoomStay roomStay);

    @Named("calculateBookingCost")
    default BigDecimal calculateBookingCost(Booking booking) {
        if (booking == null) {
            return BigDecimal.ZERO;
        }
        return booking.calculateTotalCost();
    }

    @Named("calculateRoomStayCost")
    default BigDecimal calculateRoomStayCost(RoomStay roomStay) {
        if (roomStay == null) {
            return BigDecimal.ZERO;
        }
        return roomStay.calculateTotalCost();
    }
}
