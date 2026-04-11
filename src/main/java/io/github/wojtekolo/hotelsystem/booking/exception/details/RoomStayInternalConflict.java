package io.github.wojtekolo.hotelsystem.booking.exception.details;

import java.time.LocalDate;

public record RoomStayInternalConflict(
        Long roomId,
        Long id1,
        LocalDate from1,
        LocalDate to1,
        Long id2,
        LocalDate from2,
        LocalDate to2
) {
}
