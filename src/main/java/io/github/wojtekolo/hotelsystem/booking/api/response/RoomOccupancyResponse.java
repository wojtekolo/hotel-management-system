package io.github.wojtekolo.hotelsystem.booking.api.response;

import java.time.LocalDate;
import java.util.List;

public record RoomOccupancyResponse(
        Long roomId,
        LocalDate searchFrom,
        LocalDate searchTo,
        List<OccupiedRange> occupiedRanges
) {
}
