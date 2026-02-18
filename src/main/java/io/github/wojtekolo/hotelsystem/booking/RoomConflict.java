package io.github.wojtekolo.hotelsystem.booking;

import io.github.wojtekolo.hotelsystem.room.Room;

import java.util.List;

public record RoomConflict(
        Room room,
        List<RoomStay> conflicts
) {
}
