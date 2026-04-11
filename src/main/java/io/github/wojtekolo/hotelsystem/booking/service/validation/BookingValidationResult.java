package io.github.wojtekolo.hotelsystem.booking.service.validation;

import io.github.wojtekolo.hotelsystem.booking.exception.details.RoomStayExternalConflict;
import io.github.wojtekolo.hotelsystem.booking.exception.details.RoomStayInternalConflict;
import io.github.wojtekolo.hotelsystem.booking.exception.details.RoomStayViolationDetails;
import io.github.wojtekolo.hotelsystem.booking.exception.details.IntegrityViolationDetails;
import io.github.wojtekolo.hotelsystem.booking.exception.BookingValidationException;

import java.util.List;

public record BookingValidationResult(
        List<RoomStayExternalConflict> externalConflicts,
        List<RoomStayInternalConflict> internalConflicts
) {
    public boolean hasErrors() {
        return !externalConflicts.isEmpty() ||
                !internalConflicts.isEmpty();
    }

    public BookingValidationException toException(String message, List<RoomStayViolationDetails> statusErrors, List<IntegrityViolationDetails> integrityErrors) {
        return new BookingValidationException(
                message,
                this.externalConflicts,
                this.internalConflicts,
                statusErrors,
                integrityErrors
        );
    }
}