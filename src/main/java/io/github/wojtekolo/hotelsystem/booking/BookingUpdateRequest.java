package io.github.wojtekolo.hotelsystem.booking;

import java.util.List;

public record BookingUpdateRequest(
        Long bookingId,
        List<RoomStayUpdateRequest> stays
) {
}
