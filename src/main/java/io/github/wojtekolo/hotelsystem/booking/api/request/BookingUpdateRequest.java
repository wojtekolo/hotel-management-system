package io.github.wojtekolo.hotelsystem.booking.api.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BookingUpdateRequest(
        @Valid
        @NotEmpty(message = "At least one room stay is required")
        List<RoomStayUpdateRequest> stays
) {
}
