package io.github.wojtekolo.hotelsystem.booking;

import java.util.List;

public record BookingCreateRequest(
        Long customerId,
        Long employeeId,
        List<RoomStayCreateRequest> stays
) {
}
