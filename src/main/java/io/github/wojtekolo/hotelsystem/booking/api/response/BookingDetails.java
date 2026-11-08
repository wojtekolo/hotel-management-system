package io.github.wojtekolo.hotelsystem.booking.api.response;

import io.github.wojtekolo.hotelsystem.booking.model.entity.BookingStatus;

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
        BookingStatus status,
        List<RoomStayDetails> stays
) {
}
