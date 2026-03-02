package io.github.wojtekolo.hotelsystem.booking;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RoomStayUpdateRequest(
        Long id,
        Long roomId,
        LocalDate from,
        LocalDate to,
        BigDecimal pricePerNight
) {
}
