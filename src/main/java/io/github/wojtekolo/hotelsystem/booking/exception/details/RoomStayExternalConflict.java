package io.github.wojtekolo.hotelsystem.booking.exception.details;

import java.time.LocalDate;
import java.util.List;

public record RoomStayExternalConflict(
        Long roomId,
        String roomName,
        Long roomStayId,
        LocalDate from,
        LocalDate to,
        List<ConflictingStay> conflictingStays
) {
}
