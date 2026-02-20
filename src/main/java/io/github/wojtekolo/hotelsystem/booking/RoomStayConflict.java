package io.github.wojtekolo.hotelsystem.booking;

import java.util.List;

public record RoomStayConflict(
        Long roomId,
        String roomName,
        List<RoomStayConflictDetails> roomConflictsDetails
) {
}
