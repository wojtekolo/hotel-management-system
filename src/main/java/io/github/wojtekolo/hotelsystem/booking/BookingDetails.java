package io.github.wojtekolo.hotelsystem.booking;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record BookingDetails(
        Long id,
        String customerName,
        String customerPhone,
        String loyaltyDiscount,
        LocalDateTime createTime,
        BigDecimal totalCost,
        String createBy,
        PaymentStatus paymentStatus,
        BookingStatus status,
        List<RoomStayDetails> stays
) {
}
