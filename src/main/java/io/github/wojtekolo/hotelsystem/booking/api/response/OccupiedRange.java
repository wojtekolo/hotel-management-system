package io.github.wojtekolo.hotelsystem.booking.api.response;

import java.time.LocalDate;

public record OccupiedRange(
        LocalDate from,
        LocalDate to
) {
}
