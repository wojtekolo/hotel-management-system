package io.github.wojtekolo.hotelsystem.room.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RoomCreateRequest(
        @NotBlank(message = "Room name is required")
        String name,
        @NotNull(message = "Room floor is required")
        Integer floor,
        String description,
        @NotNull(message = "Room type is required")
        Long typeId
) {
}
