package io.github.wojtekolo.hotelsystem.booking.api.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record RoomStayCreateRequest(
        @NotNull(message = "Room ID is required")
        Long roomId,
        @NotNull(message = "Start date is required")
        LocalDate from,
        @NotNull(message = "End date is required")
        LocalDate to,
        BigDecimal customPricePerNight
) {
}
