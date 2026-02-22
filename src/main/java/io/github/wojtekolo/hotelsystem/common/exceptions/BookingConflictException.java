package io.github.wojtekolo.hotelsystem.common.exceptions;

import io.github.wojtekolo.hotelsystem.booking.RoomStayConflict;
import lombok.Getter;

import java.util.List;

@Getter
public class BookingConflictException extends RuntimeException{
    List<RoomStayConflict> conflicts;

    public BookingConflictException(List<RoomStayConflict> conflicts) {
        super("Detected conflicting bookings in selected periods");
        this.conflicts = conflicts;
    }
}
