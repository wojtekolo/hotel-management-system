package io.github.wojtekolo.hotelsystem.booking;

import io.github.wojtekolo.hotelsystem.guest.Guest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "paymentStatus", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "stays", ignore = true)
    Booking toBookingEntity(BookingCreateRequest createRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "booking", ignore = true)
    @Mapping(target = "room", ignore = true)
    @Mapping(target = "pricePerNight", ignore = true)
    @Mapping(target = "activeFrom", source = "from")
    @Mapping(target = "activeTo", source = "to")
    @Mapping(target = "actualCheckIn", ignore = true)
    @Mapping(target = "actualCheckOut", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "lastUpdateTime", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "guests", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "checkedInBy", ignore = true)
    @Mapping(target = "checkedOutBy", ignore = true)
    RoomStay toRoomStayEntity(SingleRoomStayRequest roomStayRequest);

    @Mapping(target = "bookingId", source = "roomStay.booking.id")
    @Mapping(target = "roomId", source = "roomStay.room.id")
    @Mapping(target = "roomName", source = "roomStay.room.name")
    @Mapping(target = "roomType", source = "roomStay.room.type.name")
    @Mapping(target = "totalCost", source = "calculatedCost")
    @Mapping(target = "guestNames", source = "roomStay.guests")

    @Mapping(target = "createEmployeeName", source = "roomStay.createdBy.person.name")
    @Mapping(target = "createEmployeeSurname", source = "roomStay.createdBy.person.surname")

    @Mapping(target = "checkInEmployeeName", source = "roomStay.checkedInBy.person.name")
    @Mapping(target = "checkInEmployeeSurname", source = "roomStay.checkedInBy.person.surname")

    @Mapping(target = "checkOutEmployeeName", source = "roomStay.checkedOutBy.person.name")
    @Mapping(target = "checkOutEmployeeSurname", source = "roomStay.checkedOutBy.person.surname")
    RoomStayDetails toRoomStayDetails(RoomStay roomStay, BigDecimal calculatedCost);

    default String mapGuestToString(Guest guest) {
        if (guest == null || guest.getPerson() == null) return null;
        return guest.getPerson().getName() + " " + guest.getPerson().getSurname();
    }
}
