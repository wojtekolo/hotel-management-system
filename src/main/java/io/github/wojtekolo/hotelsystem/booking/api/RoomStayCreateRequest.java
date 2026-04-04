package io.github.wojtekolo.hotelsystem.booking.api;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record RoomStayCreateRequest(
        Long roomId,
        LocalDate from,
        LocalDate to,
        BigDecimal customPricePerNight
) {
}
