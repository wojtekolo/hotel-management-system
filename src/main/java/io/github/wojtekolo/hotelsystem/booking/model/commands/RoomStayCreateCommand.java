package io.github.wojtekolo.hotelsystem.booking.model.commands;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RoomStayCreateCommand(
        Long roomId,
        LocalDate from,
        LocalDate to,
        BigDecimal customPricePerNight
) {
}
