package io.github.wojtekolo.hotelsystem.booking;

public record RoomStayBadStatusDetails(
        Long id,
        RoomStayStatus status,
        RoomStayErrorCode errorCode
) {
}
