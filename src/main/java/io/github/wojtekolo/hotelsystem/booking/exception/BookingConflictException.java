package io.github.wojtekolo.hotelsystem.booking.exception;

import io.github.wojtekolo.hotelsystem.booking.api.ExternalRoomStayConflict;
import lombok.Getter;

import java.util.List;

@Getter
public class BookingConflictException extends RuntimeException{
    List<ExternalRoomStayConflict> conflicts;

    public BookingConflictException(List<ExternalRoomStayConflict> conflicts) {
        super("Detected conflicting bookings in selected periods");
        this.conflicts = conflicts;
    }
}
