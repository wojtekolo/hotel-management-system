package io.github.wojtekolo.hotelsystem.booking.api;

import java.util.List;

public record BookingConflictResponse(
        String message,
        List<ExternalRoomStayConflict> conflictingRooms
) {
}
