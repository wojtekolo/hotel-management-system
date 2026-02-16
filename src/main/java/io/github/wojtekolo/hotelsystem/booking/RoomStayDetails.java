package io.github.wojtekolo.hotelsystem.booking;

import jakarta.persistence.Column;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record RoomStayDetails(
        Long id,
        Long bookingId,
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
        List<String> guestNames,
        String createEmployeeName,
        String createEmployeeSurname,
        String checkInEmployeeName,
        String checkInEmployeeSurname,
        String checkOutEmployeeName,
        String checkOutEmployeeSurname
) {
}
