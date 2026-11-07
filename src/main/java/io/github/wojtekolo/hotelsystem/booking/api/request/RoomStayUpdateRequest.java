package io.github.wojtekolo.hotelsystem.booking.api.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record RoomStayUpdateRequest(
        Long id,
        @NotNull(message = "Room ID is required")
        Long roomId,
        @NotNull(message = "Start date is required")
        LocalDate from,
        @NotNull(message = "End date is required")
        LocalDate to
) {
}
