package io.github.wojtekolo.hotelsystem.booking.api.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record BookingCreateRequest(
        @NotNull(message = "Customer ID is required")
        Long customerId,
        @NotNull(message = "Employee ID is required")
        Long employeeId,
        @Valid
        @NotEmpty(message = "At least one room stay is required")
        List<RoomStayCreateRequest> stays
) {
}
