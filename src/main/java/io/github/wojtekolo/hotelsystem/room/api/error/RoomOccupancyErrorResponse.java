package io.github.wojtekolo.hotelsystem.room.api.error;

import io.github.wojtekolo.hotelsystem.booking.exception.details.RoomOccupancyErrorDetails;

import java.util.List;

public record RoomOccupancyErrorResponse(
        String message,
        List<RoomOccupancyErrorDetails> details
) {
}
