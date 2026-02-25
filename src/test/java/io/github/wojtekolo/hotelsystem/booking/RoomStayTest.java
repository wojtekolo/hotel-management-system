package io.github.wojtekolo.hotelsystem.booking;

import io.github.wojtekolo.hotelsystem.room.Room;
import io.github.wojtekolo.hotelsystem.room.RoomTestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class RoomStayTest {

    @Test
    public void should_apply_discount_when_custom_price_is_null() {
//        given
        Room room = RoomTestUtils.aValidRoom(RoomTestUtils.aValidType().pricePerNight(BigDecimal.valueOf(500)).build()).build();

//        when
        BigDecimal result = RoomStay.calculatePricePerNight(room, BigDecimal.valueOf(0.1), null);

//        then
        assertThat(result).isNotNull();
        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(450));
    }

    @Test
    public void should_not_apply_discount_when_custom_price() {
//        given
        Room room = RoomTestUtils.aValidRoom(RoomTestUtils.aValidType().pricePerNight(BigDecimal.valueOf(500)).build()).build();

//        when
        BigDecimal result = RoomStay.calculatePricePerNight(room, BigDecimal.valueOf(0.1), BigDecimal.valueOf(300));

//        then
        assertThat(result).isNotNull();
        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(300));
    }

    @Test
    public void should_fail_when_discount_is_less_than_0() {
//        given
        Room room = RoomTestUtils.aValidRoom().build();

//        when and then
        assertThatThrownBy(() -> RoomStay.calculatePricePerNight(room, BigDecimal.valueOf(-0.1), BigDecimal.valueOf(300)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_succeed_when_discount_is_equal_to_0() {
//        given
        Room room = RoomTestUtils.aValidRoom().build();

//        when
        BigDecimal result = RoomStay.calculatePricePerNight(room, BigDecimal.ZERO, BigDecimal.valueOf(300));

//        then
        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(300));
    }

    @Test
    public void should_succeed_when_discount_is_equal_to_1() {
//        given
        Room room = RoomTestUtils.aValidRoom().build();

//        when and then
        BigDecimal result = RoomStay.calculatePricePerNight(room, BigDecimal.valueOf(1), null);

        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    public void should_fail_when_discount_is_greater_than_1() {
//        given
        Room room = RoomTestUtils.aValidRoom().build();

//        when and then
        assertThatThrownBy(() -> RoomStay.calculatePricePerNight(room, BigDecimal.valueOf(1.1), BigDecimal.valueOf(300)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_fail_when_custom_price_is_negative() {
//        given
        Room room = RoomTestUtils.aValidRoom().build();

//        when and then
        assertThatThrownBy(() -> RoomStay.calculatePricePerNight(room, BigDecimal.valueOf(0.1), BigDecimal.valueOf(-300)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_create_planned_room_stay_with_discounted_price(){
//        given
        LocalDate today = LocalDate.now();
        Room room = RoomTestUtils.aValidRoom()
                .type(RoomTestUtils.aValidType().pricePerNight(BigDecimal.valueOf(500)).build())
                .build();
        BigDecimal discount = BigDecimal.valueOf(0.1);

//        when
        RoomStay roomStay = RoomStay.createPlanned(null, room, discount, null,
                today.plusDays(5), today.plusDays(10), null);

//        then
        assertThat(roomStay.getPricePerNight()).isEqualByComparingTo(BigDecimal.valueOf(450));
        assertThat(roomStay.getStatus()).isEqualTo(RoomStayStatus.PLANNED);
    }

    @Test
    public void should_handle_rounding_correctly() {
//        given
//        133.33 * 0.9 = 119.997
        Room room = RoomTestUtils.aValidRoom(
                RoomTestUtils.aValidType().pricePerNight(BigDecimal.valueOf(133.33)).build()
        ).build();

//        when
        BigDecimal result = RoomStay.calculatePricePerNight(room, BigDecimal.valueOf(0.1), null);

//        then
        assertThat(result).isEqualByComparingTo(new BigDecimal("120.00"));
    }

    @Test
    public void should_create_planned_room_stay_with_custom_price_ignoring_discount(){
//        given
        LocalDate today = LocalDate.now();
        Room room = RoomTestUtils.aValidRoom()
                .type(RoomTestUtils.aValidType().pricePerNight(BigDecimal.valueOf(500)).build())
                .build();
        BigDecimal discount = BigDecimal.valueOf(0.1);
        BigDecimal customPrice = BigDecimal.valueOf(600);

//        when
        RoomStay roomStay = RoomStay.createPlanned(null, room, discount, null,
                today.plusDays(5), today.plusDays(10), customPrice);

//        then
        assertThat(roomStay.getPricePerNight()).isEqualByComparingTo(customPrice);
        assertThat(roomStay.getStatus()).isEqualTo(RoomStayStatus.PLANNED);
    }



}