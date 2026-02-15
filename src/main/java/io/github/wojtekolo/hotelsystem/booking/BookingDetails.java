package io.github.wojtekolo.hotelsystem.booking;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record BookingDetails(
        Long id,
        Long customerId,
        String customerName,
        String customerSurname,
        String customerPhone,
        LocalDateTime createTime,
        BigDecimal totalCost,
        PaymentStatus paymentStatus,
        BookingStatus status,
        List<RoomStayDetails> stays
) {
}
