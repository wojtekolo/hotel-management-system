package io.github.wojtekolo.hotelsystem.booking;

import io.github.wojtekolo.hotelsystem.customer.CustomerMapper;
import io.github.wojtekolo.hotelsystem.employee.EmployeeMapper;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = {CustomerMapper.class, EmployeeMapper.class},
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface BookingMapper {

    @Mapping(target = "totalCost", source = "totalCost")
    @Mapping(target = "stays", source = "stays")
    @Mapping(target = "customerName", source = "booking.customer")
    @Mapping(target = "customerPhone", source = "booking.customer.privatePhone")
    @Mapping(target = "loyaltyDiscount", source = "booking.customer.loyaltyStatus.discount")
    BookingDetails toBookingDetails(Booking booking, BigDecimal totalCost, List<RoomStayDetails> stays);

    @Mapping(target = "roomId", source = "roomStay.room.id")
    @Mapping(target = "roomName", source = "roomStay.room.name")
    @Mapping(target = "roomType", source = "roomStay.room.type.name")
    @Mapping(target = "totalCost", source = "calculatedTotalCost")

    @Mapping(target = "createBy", source = "roomStay.createBy")
    @Mapping(target = "checkInBy", source = "roomStay.checkInBy")
    @Mapping(target = "checkOutBy", source = "roomStay.checkOutBy")
    RoomStayDetails toRoomStayDetails(RoomStay roomStay, BigDecimal calculatedTotalCost);

    @Mapping(target = "bookingId", source = "booking.id")
    @Mapping(target = "roomStayId", source = "id")
    @Mapping(target = "from", source = "activeFrom")
    @Mapping(target = "to", source = "activeTo")
    RoomStayConflictDetails toRoomStayConflictDetails(RoomStay roomStay);
}
