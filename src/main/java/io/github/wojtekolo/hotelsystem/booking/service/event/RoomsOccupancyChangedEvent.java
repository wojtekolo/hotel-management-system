package io.github.wojtekolo.hotelsystem.booking.service.event;

import java.util.Set;

public record RoomsOccupancyChangedEvent(
        Set<Long> roomIds
) {
}