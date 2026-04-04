package io.github.wojtekolo.hotelsystem.booking;

import java.time.LocalDate;
import java.util.List;

public record ExternalRoomStayConflict(
        Long roomId,
        String roomName,
        Long roomStayId,
        LocalDate from,
        LocalDate to,
        List<RoomStayConflictDetails> roomConflictsDetails
) {
}
