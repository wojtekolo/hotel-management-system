package io.github.wojtekolo.hotelsystem.booking.api;

import java.time.LocalDate;

public record RoomStayConflictDetails(
        Long bookingId,
        Long roomStayId,
        LocalDate from,
        LocalDate to
) {
}
