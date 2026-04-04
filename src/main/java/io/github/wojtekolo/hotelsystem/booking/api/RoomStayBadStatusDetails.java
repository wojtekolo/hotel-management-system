package io.github.wojtekolo.hotelsystem.booking.api;

import io.github.wojtekolo.hotelsystem.booking.exception.RoomStayErrorCode;
import io.github.wojtekolo.hotelsystem.booking.model.RoomStayStatus;

public record RoomStayBadStatusDetails(
        Long id,
        RoomStayStatus status,
        RoomStayErrorCode errorCode
) {
}
