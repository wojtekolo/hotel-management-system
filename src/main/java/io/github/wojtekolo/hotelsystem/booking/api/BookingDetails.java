package io.github.wojtekolo.hotelsystem.booking.api;

import io.github.wojtekolo.hotelsystem.booking.model.BookingStatus;
import io.github.wojtekolo.hotelsystem.booking.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record BookingDetails(
        Long id,
        String customerFullName,
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
