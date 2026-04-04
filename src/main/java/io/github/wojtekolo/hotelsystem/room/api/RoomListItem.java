package io.github.wojtekolo.hotelsystem.room.api;

import io.github.wojtekolo.hotelsystem.room.model.LifecycleStatus;
import io.github.wojtekolo.hotelsystem.room.model.OperationalStatus;
import java.math.BigDecimal;

public record RoomListItem(
        Long id,
        String name,
        BigDecimal pricePerNight,
        Integer floor,
        String type,
        OperationalStatus operationalStatus,
        LifecycleStatus lifecycleStatus,
        Integer capacity
) {
}
