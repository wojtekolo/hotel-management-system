package io.github.wojtekolo.hotelsystem.booking;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record SingleRoomStayRequest(
        Long roomId,
        LocalDate from,
        LocalDate to,
        BigDecimal customPricePerNight
) {
}
