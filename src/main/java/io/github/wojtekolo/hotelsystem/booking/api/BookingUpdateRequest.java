package io.github.wojtekolo.hotelsystem.booking.api;

import java.util.List;

public record BookingUpdateRequest(
        Long bookingId,
        Long employeeId,
        List<RoomStayUpdateRequest> stays
) {
}
