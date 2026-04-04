package io.github.wojtekolo.hotelsystem.booking.exception;

import lombok.Getter;

@Getter
public class BookingStatusException extends RuntimeException {
    private final BookingErrorCode errorCode;
    private final Long bookingId;

    public BookingStatusException(String message, BookingErrorCode errorCode, Long bookingId) {
        super(message);
        this.errorCode = errorCode;
        this.bookingId = bookingId;
    }
}
