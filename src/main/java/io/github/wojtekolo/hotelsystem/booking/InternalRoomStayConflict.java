package io.github.wojtekolo.hotelsystem.booking;

import java.time.LocalDate;

public record InternalRoomStayConflict(
        Long roomId,
        LocalDate from1,
        LocalDate to1,
        LocalDate from2,
        LocalDate to2
) {
}
