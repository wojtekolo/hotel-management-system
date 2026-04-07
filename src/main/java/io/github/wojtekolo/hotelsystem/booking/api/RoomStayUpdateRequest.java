package io.github.wojtekolo.hotelsystem.booking.api;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RoomStayUpdateRequest(
        @NotNull(message = "Room Stay ID is required")
        Long id,
        @NotNull(message = "Room ID is required")
        Long roomId,
        @NotNull(message = "Start date is required")
        LocalDate from,
        @NotNull(message = "End date is required")
        LocalDate to,
        BigDecimal pricePerNight
) {
}
