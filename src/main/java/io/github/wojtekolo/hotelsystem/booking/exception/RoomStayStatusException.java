package io.github.wojtekolo.hotelsystem.booking.exception;

import io.github.wojtekolo.hotelsystem.booking.api.RoomStayBadStatusDetails;
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
