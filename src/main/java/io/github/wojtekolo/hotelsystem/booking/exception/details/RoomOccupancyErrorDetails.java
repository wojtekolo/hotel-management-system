package io.github.wojtekolo.hotelsystem.booking.exception.details;

import java.time.LocalDate;

public record RoomOccupancyErrorDetails(
        LocalDate date,
        RoomOccupancyErrorCode errorCode
) {
}
