package io.github.wojtekolo.hotelsystem.booking;

import java.util.List;

public record BookingRequestConflictResponse(
        String message,
        List<InternalRoomStayConflict> conflicts
) {
}
