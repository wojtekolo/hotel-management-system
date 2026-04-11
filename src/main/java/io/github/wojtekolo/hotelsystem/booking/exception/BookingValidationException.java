package io.github.wojtekolo.hotelsystem.booking.exception;

import io.github.wojtekolo.hotelsystem.booking.exception.details.RoomStayInternalConflict;
import io.github.wojtekolo.hotelsystem.booking.exception.details.RoomStayExternalConflict;
import io.github.wojtekolo.hotelsystem.booking.exception.details.RoomStayViolationDetails;
import io.github.wojtekolo.hotelsystem.booking.exception.details.IntegrityViolationDetails;
import lombok.Getter;

import java.util.List;

@Getter
public class BookingValidationException extends RuntimeException {
    private final List<RoomStayExternalConflict> externalConflicts;
    private final List<RoomStayInternalConflict> internalConflicts;
    private final List<RoomStayViolationDetails> roomStayViolationsDetails;
    private final List<IntegrityViolationDetails> integrityViolationsDetails;

    public BookingValidationException(String message, List<RoomStayExternalConflict> externalConflicts, List<RoomStayInternalConflict> internalConflicts, List<RoomStayViolationDetails> roomStayViolationsDetails, List<IntegrityViolationDetails> IntegrityViolationsDetails) {
        super(message);
        this.externalConflicts = externalConflicts;
        this.internalConflicts = internalConflicts;
        this.roomStayViolationsDetails = roomStayViolationsDetails;
        this.integrityViolationsDetails = IntegrityViolationsDetails;
    }
}
