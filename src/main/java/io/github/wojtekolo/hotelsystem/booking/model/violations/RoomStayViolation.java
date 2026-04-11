package io.github.wojtekolo.hotelsystem.booking.model.violations;

import io.github.wojtekolo.hotelsystem.booking.model.entity.RoomStayStatus;

import java.util.Map;

public record RoomStayViolation(
        Long stayId,
        RoomStayStatus currentStatus,
        RoomStayViolationReason reason,
        Map<String, Object> context
) {}