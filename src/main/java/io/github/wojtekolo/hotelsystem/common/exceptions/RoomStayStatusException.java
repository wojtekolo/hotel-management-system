package io.github.wojtekolo.hotelsystem.common.exceptions;

import io.github.wojtekolo.hotelsystem.booking.BookingErrorCode;
import io.github.wojtekolo.hotelsystem.booking.RoomStayBadStatusDetails;
import lombok.Getter;

import java.util.List;

@Getter
public class RoomStayStatusException extends RuntimeException {
    private final Long bookingId;
    private final List<RoomStayBadStatusDetails> details;

    public RoomStayStatusException(String message, Long bookingId, List<RoomStayBadStatusDetails> details) {
        super(message);
        this.bookingId = bookingId;
        this.details = details;
    }
}
