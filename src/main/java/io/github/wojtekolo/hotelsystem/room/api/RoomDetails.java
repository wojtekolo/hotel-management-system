package io.github.wojtekolo.hotelsystem.room.api;

import io.github.wojtekolo.hotelsystem.room.model.LifecycleStatus;
import io.github.wojtekolo.hotelsystem.room.model.OperationalStatus;

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
