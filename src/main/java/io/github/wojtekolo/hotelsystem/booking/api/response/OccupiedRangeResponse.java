package io.github.wojtekolo.hotelsystem.booking.api.response;

import java.time.LocalDate;

public record OccupiedRangeResponse(
        LocalDate from,
        LocalDate to
) {
}
