package io.github.wojtekolo.hotelsystem.common.exceptions;

import io.github.wojtekolo.hotelsystem.booking.InternalRoomStayConflict;
import lombok.Getter;

import java.util.List;

@Getter
public class BookingRequestConflictException extends RuntimeException{
    List<InternalRoomStayConflict> conflicts;

    public BookingRequestConflictException(List<InternalRoomStayConflict> conflicts) {
        super("Detected conflict in received request");
        this.conflicts = conflicts;
    }
}