package io.github.wojtekolo.hotelsystem.room.dtos;

import java.math.BigDecimal;

public record RoomListItem(
        Long id,
        String name,
        BigDecimal pricePerNight,
        Integer floor,
        String type,
        String status
) {
}
