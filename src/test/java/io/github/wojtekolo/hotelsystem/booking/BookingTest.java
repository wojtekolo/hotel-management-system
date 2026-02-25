package io.github.wojtekolo.hotelsystem.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BookingTest {
    @Test
    public void should_calculate_total_cost() {
        // given
        LocalDate today = LocalDate.now();
        Booking booking = BookingTestUtils.aValidBooking().build();

        RoomStay stay1 = BookingTestUtils.aValidRoomStay()
                                         .booking(booking)
                                         .pricePerNight(BigDecimal.valueOf(100))
                                         .activeFrom(today.plusDays(10))
                                         .activeTo(today.plusDays(15))
                                         .build();

        RoomStay stay2 = BookingTestUtils.aValidRoomStay()
                                         .booking(booking)
                                         .pricePerNight(BigDecimal.valueOf(200))
                                         .activeFrom(today.plusDays(10))
                                         .activeTo(today.plusDays(20))
                                         .build();

        booking.addStay(stay1);
        booking.addStay(stay2);

        // when
        BigDecimal total = booking.calculateTotalCost();

        // then
        assertThat(total).isEqualByComparingTo(BigDecimal.valueOf(2500));
    }
}