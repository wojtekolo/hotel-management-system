package io.github.wojtekolo.hotelsystem.booking.exception;

import io.github.wojtekolo.hotelsystem.booking.exception.details.RoomOccupancyErrorDetails;
import lombok.Getter;

import java.util.List;

public class InvalidBookingDateException extends RuntimeException {
    @Getter
    private final List<RoomOccupancyErrorDetails> details;
    public InvalidBookingDateException(List<RoomOccupancyErrorDetails> details, String message) {
        super(message);
        this.details = details;
    }

}
