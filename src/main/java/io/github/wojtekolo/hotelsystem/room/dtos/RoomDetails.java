package io.github.wojtekolo.hotelsystem.room.dtos;

import io.github.wojtekolo.hotelsystem.room.LifecycleStatus;
import io.github.wojtekolo.hotelsystem.room.OperationalStatus;

public record RoomDetails(
        Long id,
        String name,
        Integer floor,
        String description,
        RoomTypeDto type,
        LifecycleStatus lifecycleStatus,
        OperationalStatus operationalStatus
) {
}
