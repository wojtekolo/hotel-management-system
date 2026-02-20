package io.github.wojtekolo.hotelsystem.booking;

import java.util.List;

public record BookingConflictResponse(
        String message,
        List<RoomStayConflict> conflictingRooms
) {
}
