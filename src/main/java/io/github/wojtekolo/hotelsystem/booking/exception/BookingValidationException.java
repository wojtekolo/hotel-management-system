package io.github.wojtekolo.hotelsystem.booking.exception;

import io.github.wojtekolo.hotelsystem.booking.api.InternalRoomStayConflict;
import io.github.wojtekolo.hotelsystem.booking.api.ExternalRoomStayConflict;
import io.github.wojtekolo.hotelsystem.booking.api.RoomStayBadStatusDetails;
import lombok.Getter;

import java.util.List;

@Getter
public class BookingValidationException extends RuntimeException {
    private final List<ExternalRoomStayConflict> externalConflicts;
    private final List<InternalRoomStayConflict> internalConflicts;
    private final List<RoomStayBadStatusDetails> badStatusDetails;

    public BookingValidationException(String message, List<ExternalRoomStayConflict> externalConflicts, List<InternalRoomStayConflict> internalConflicts, List<RoomStayBadStatusDetails> badStatusDetails) {
        super(message);
        this.externalConflicts = externalConflicts;
        this.internalConflicts = internalConflicts;
        this.badStatusDetails = badStatusDetails;
    }
}
