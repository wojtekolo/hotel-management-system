package io.github.wojtekolo.hotelsystem.booking.service.occupancy;

import io.github.wojtekolo.hotelsystem.booking.api.response.OccupiedRange;

import java.time.LocalDate;
import java.util.List;

public record RoomOccupancyData(
        Long roomId,
        LocalDate searchFrom,
        LocalDate searchTo,
        List<OccupiedRange> occupiedRanges
) {
}
