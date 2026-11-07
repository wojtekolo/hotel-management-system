package io.github.wojtekolo.hotelsystem.booking.model.commands;

import java.time.LocalDate;

public record RoomStayUpdateCommand(
        Long stayId,
        Long roomId,
        LocalDate from,
        LocalDate to
) {
}
