package io.github.wojtekolo.hotelsystem.booking.service.loading;

import io.github.wojtekolo.hotelsystem.booking.api.request.BookingCreateRequest;
import io.github.wojtekolo.hotelsystem.booking.api.request.BookingUpdateRequest;
import io.github.wojtekolo.hotelsystem.booking.api.request.RoomStayCreateRequest;
import io.github.wojtekolo.hotelsystem.booking.api.request.RoomStayUpdateRequest;
import io.github.wojtekolo.hotelsystem.booking.model.entity.Booking;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class BookingRequestHelper {


    public Set<Long> collectAllRoomIds(BookingCreateRequest request) {
        return request.stays().stream().map(RoomStayCreateRequest::roomId).collect(Collectors.toSet());
    }

    public Set<Long> collectAllRoomIds(Booking booking, BookingUpdateRequest request) {
        Set<Long> ids = request.stays().stream().map(RoomStayUpdateRequest::roomId).collect(Collectors.toSet());
        ids.addAll(booking.getStays().stream().map(stay -> stay.getRoom().getId()).collect(Collectors.toSet()));
        return ids;
    }
}
