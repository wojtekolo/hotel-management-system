package io.github.wojtekolo.hotelsystem.room.dtos;

public record RoomDetails(
        Long id,
        String name,
        Integer floor,
        String description,
        RoomTypeDto type,
        RoomStatusDto status
) {
}
