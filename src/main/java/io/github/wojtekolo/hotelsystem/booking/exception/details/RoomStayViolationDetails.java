package io.github.wojtekolo.hotelsystem.booking.exception.details;

import io.github.wojtekolo.hotelsystem.booking.model.entity.RoomStayStatus;

import java.util.Map;

public record RoomStayViolationDetails(
        Long stayId,
        RoomStayStatus currentStatus,
        RoomStayViolationCode code,
        Map<String, Object> context
) {
}
