package io.github.wojtekolo.hotelsystem.room.api;

import java.math.BigDecimal;

public record RoomTypeDto(
        Long id,
        String name,
        BigDecimal pricePerNight,
        String description,
        Integer capacity
) {
}
