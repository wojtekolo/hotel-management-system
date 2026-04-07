package io.github.wojtekolo.hotelsystem.booking.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record BookingUpdateRequest(
        @NotNull(message = "Booking ID is required")
        Long bookingId,
        @NotNull(message = "Employee ID is required")
        Long employeeId,
        @Valid
        @NotEmpty(message = "At least one room stay is required")
        List<RoomStayUpdateRequest> stays
) {
}
