package io.github.wojtekolo.hotelsystem.booking.service.processing;

import io.github.wojtekolo.hotelsystem.booking.exception.details.RoomStayViolationDetails;

import java.util.List;
import java.util.Set;

public record BookingProcessingResult(
        List<RoomStayViolationDetails> errors,
        Set<Long> affectedRoomIds
) {}
