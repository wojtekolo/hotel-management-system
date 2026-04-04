package io.github.wojtekolo.hotelsystem.booking.api;


import io.github.wojtekolo.hotelsystem.booking.model.RoomStayStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record RoomStayDetails(
        Long id,
        Long roomId,
        String roomName,
        String roomType,
        LocalDateTime actualCheckIn,
        LocalDateTime actualCheckOut,
        LocalDate activeFrom,
        LocalDate activeTo,
        BigDecimal pricePerNight,
        BigDecimal totalCost,
        RoomStayStatus status,
        String createBy,
        String checkInBy,
        String checkOutBy
) {
}
