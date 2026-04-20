package io.github.wojtekolo.hotelsystem.room.api;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record OccupancyQuery(
        @NotNull (message = "End date is required")
        LocalDate from,

        @NotNull (message = "End date is required")
        LocalDate to
) {}
