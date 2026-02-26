package io.github.wojtekolo.hotelsystem.booking;

import java.util.List;

public record BookingValidationResponse(
        String message,
        List<ExternalRoomStayConflict> externalConflicts,
        List<InternalRoomStayConflict> internalConflicts,
        List<RoomStayBadStatusDetails> badStatusDetails

) {
}
