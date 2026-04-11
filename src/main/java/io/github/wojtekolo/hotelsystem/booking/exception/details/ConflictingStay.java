package io.github.wojtekolo.hotelsystem.booking.exception.details;

import java.time.LocalDate;

public record ConflictingStay(
        Long bookingId,
        Long roomStayId,
        LocalDate from,
        LocalDate to
) {
}
