package io.github.wojtekolo.hotelsystem.booking.service.loading;

import io.github.wojtekolo.hotelsystem.room.model.Room;

import java.util.Map;
import java.util.Set;

public record RoomLoadResult(
        Map<Long, Room> rooms,
        Set<Long> missingIds
) {
}
