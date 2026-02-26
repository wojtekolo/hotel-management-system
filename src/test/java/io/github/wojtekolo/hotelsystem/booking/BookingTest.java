package io.github.wojtekolo.hotelsystem.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BookingTest {
    @Test
    public void should_calculate_total_cost() {
        // given
        LocalDate today = LocalDate.now();
        Booking booking = Booking.builder().stays(new ArrayList<>()).build();

        RoomStay stay1 = RoomStay.builder()
                                 .pricePerNight(BigDecimal.valueOf(100))
                                 .activeFrom(today.plusDays(10))
                                 .activeTo(today.plusDays(15))
                                 .status(RoomStayStatus.PLANNED)
                                 .build();

        RoomStay stay2 = RoomStay.builder()
                                 .pricePerNight(BigDecimal.valueOf(200))
                                 .activeFrom(today.plusDays(10))
                                 .activeTo(today.plusDays(20))
                                 .status(RoomStayStatus.PLANNED)
                                 .build();

        booking.addStay(stay1);
        booking.addStay(stay2);

        // when
        BigDecimal total = booking.calculateTotalCost();

        // then
        assertThat(total).isEqualByComparingTo(BigDecimal.valueOf(2500));
    }

    @Test
    public void should_not_count_cancelled_or_no_show_stays_when_calculating_total() {
        // given
        LocalDate today = LocalDate.now();
        Booking booking = Booking.builder().stays(new ArrayList<>()).build();

        RoomStay stay1 = RoomStay.builder()
                                 .pricePerNight(BigDecimal.valueOf(100))
                                 .activeFrom(today.plusDays(10))
                                 .activeTo(today.plusDays(20))
                                 .status(RoomStayStatus.PLANNED)
                                 .build();

        RoomStay stay2 = RoomStay.builder()
                                 .pricePerNight(BigDecimal.valueOf(200))
                                 .activeFrom(today.plusDays(10))
                                 .activeTo(today.plusDays(20))
                                 .status(RoomStayStatus.CANCELLED)
                                 .build();

        RoomStay stay3 = RoomStay.builder()
                                 .pricePerNight(BigDecimal.valueOf(200))
                                 .activeFrom(today.plusDays(10))
                                 .activeTo(today.plusDays(20))
                                 .status(RoomStayStatus.NOSHOW)
                                 .build();

        booking.addStay(stay1);
        booking.addStay(stay2);
        booking.addStay(stay3);

        // when
        BigDecimal total = booking.calculateTotalCost();

        // then
        assertThat(total).isEqualByComparingTo(BigDecimal.valueOf(1000));
    }
}