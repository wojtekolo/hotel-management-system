package io.github.wojtekolo.hotelsystem.room.dtos;

import io.github.wojtekolo.hotelsystem.room.LifecycleStatus;
import io.github.wojtekolo.hotelsystem.room.OperationalStatus;
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
