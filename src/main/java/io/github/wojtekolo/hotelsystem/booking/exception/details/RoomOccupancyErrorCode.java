package io.github.wojtekolo.hotelsystem.booking.exception.details;

public enum RoomOccupancyErrorCode {
    CANNOT_CHECK_IN_THE_PAST,
    START_DATE_AFTER_END_DATE,
    DATE_AFTER_LIMIT
}
