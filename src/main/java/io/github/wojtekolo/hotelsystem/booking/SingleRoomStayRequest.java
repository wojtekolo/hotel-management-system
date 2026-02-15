package io.github.wojtekolo.hotelsystem.booking;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SingleRoomStayRequest(
        Long roomId,
        LocalDate from,
        LocalDate to,
        BigDecimal customPricePerNight
) {
}
