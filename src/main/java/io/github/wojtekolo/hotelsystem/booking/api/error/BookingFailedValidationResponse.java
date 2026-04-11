package io.github.wojtekolo.hotelsystem.booking.api.error;

import io.github.wojtekolo.hotelsystem.booking.exception.details.IntegrityViolationDetails;
import io.github.wojtekolo.hotelsystem.booking.exception.details.RoomStayExternalConflict;
import io.github.wojtekolo.hotelsystem.booking.exception.details.RoomStayInternalConflict;
import io.github.wojtekolo.hotelsystem.booking.exception.details.RoomStayViolationDetails;

import java.util.List;

public record BookingFailedValidationResponse(
        String message,
        List<RoomStayExternalConflict> externalConflicts,
        List<RoomStayInternalConflict> internalConflicts,
        List<RoomStayViolationDetails> violationsDetails,
        List<IntegrityViolationDetails> integrityViolationsDetails
) {
}
