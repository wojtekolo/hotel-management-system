package io.github.wojtekolo.hotelsystem.booking;

import io.github.wojtekolo.hotelsystem.booking.model.RoomStay;
import io.github.wojtekolo.hotelsystem.booking.model.RoomStayStatus;
import io.github.wojtekolo.hotelsystem.room.Room;
import io.github.wojtekolo.hotelsystem.room.RoomTestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class RoomStayTest {

    private final LocalDate referenceDate=LocalDate.of(2025, 1, 1);

    @Test
    public void should_apply_discount_when_custom_price_is_null() {
//        given
        BigDecimal roomPrice = BigDecimal.valueOf(500);
        BigDecimal discount = BigDecimal.valueOf(0.1);
        BigDecimal customPrice = null;
//        when
        BigDecimal result = RoomStay.calculatePricePerNight(roomPrice, discount, customPrice);

//        then
        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(450));
    }

    @Test
    public void should_not_apply_discount_when_custom_price() {
//        given
        BigDecimal roomPrice = BigDecimal.valueOf(500);
        BigDecimal discount = BigDecimal.valueOf(0.1);
        BigDecimal customPrice = BigDecimal.valueOf(300);
//        when
        BigDecimal result = RoomStay.calculatePricePerNight(roomPrice, discount, customPrice);

//        then
        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(300));
    }

    @Test
    public void should_fail_when_discount_is_less_than_0() {
//        given
        BigDecimal roomPrice = BigDecimal.valueOf(100);
        BigDecimal discount = BigDecimal.valueOf(-1);
        BigDecimal customPrice = BigDecimal.valueOf(300);
//        when and then
        assertThatThrownBy(() -> RoomStay.calculatePricePerNight(roomPrice, discount, customPrice))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_succeed_when_discount_is_equal_to_0() {
//        given
        BigDecimal roomPrice = BigDecimal.valueOf(100);
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal customPrice = BigDecimal.valueOf(300);
//        when
        BigDecimal result = RoomStay.calculatePricePerNight(roomPrice, discount, customPrice);

//        then
        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(300));
    }

    @Test
    public void should_succeed_when_discount_is_equal_to_1() {
//        given
        BigDecimal roomPrice = BigDecimal.valueOf(100);
        BigDecimal discount = BigDecimal.valueOf(1);
        BigDecimal customPrice = null;
//        when
        BigDecimal result = RoomStay.calculatePricePerNight(roomPrice, discount, customPrice);

//        then
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    public void should_fail_when_discount_is_greater_than_1() {
//        given
        BigDecimal roomPrice = BigDecimal.valueOf(100);
        BigDecimal discount = BigDecimal.valueOf(2);
        BigDecimal customPrice = null;
//        when and then
        assertThatThrownBy(() -> RoomStay.calculatePricePerNight(roomPrice, discount, customPrice))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_fail_when_custom_price_is_negative() {
//        given
        BigDecimal roomPrice = BigDecimal.valueOf(100);
        BigDecimal discount = BigDecimal.valueOf(0.1);
        BigDecimal customPrice = BigDecimal.valueOf(-300);

//        when and then
        assertThatThrownBy(() -> RoomStay.calculatePricePerNight(roomPrice, discount, customPrice))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_create_planned_room_stay_with_discounted_price() {
//        given
        Room room = Room.builder()
                                 .type(RoomTestUtils.aValidType().pricePerNight(BigDecimal.valueOf(500)).build())
                                 .build();
        BigDecimal discount = BigDecimal.valueOf(0.1);

//        when
        RoomStay roomStay = RoomStay.createPlanned(null, room, discount, null,
                referenceDate.plusDays(5), referenceDate.plusDays(10), null);

//        then
        assertThat(roomStay.getPricePerNight()).isEqualByComparingTo(BigDecimal.valueOf(450));
        assertThat(roomStay.getStatus()).isEqualTo(RoomStayStatus.PLANNED);
    }

    @Test
    public void should_handle_rounding_correctly() {
//        given
//        133.33 * 0.9 = 119.997
        BigDecimal discount = new BigDecimal("0.1");
        BigDecimal roomPrice = new BigDecimal("133.33");
        BigDecimal customPrice = null;

//        when
        BigDecimal result = RoomStay.calculatePricePerNight(roomPrice, discount, customPrice);

//        then
        assertThat(result).isEqualByComparingTo(new BigDecimal("120.00"));
    }

    @Test
    public void should_create_planned_room_stay_with_custom_price_ignoring_discount() {
//        given
        Room room = Room.builder().type(RoomTestUtils.aValidType().pricePerNight(BigDecimal.valueOf(500)).build()).build();

        BigDecimal discount = BigDecimal.valueOf(0.1);
        BigDecimal customPrice = BigDecimal.valueOf(600);

//        when
        RoomStay roomStay = RoomStay.createPlanned(null, room, discount, null,
                referenceDate.plusDays(5), referenceDate.plusDays(10), customPrice);

//        then
        assertThat(roomStay.getPricePerNight()).isEqualByComparingTo(customPrice);
        assertThat(roomStay.getStatus()).isEqualTo(RoomStayStatus.PLANNED);
    }

    @Test
    public void should_calculate_total_cost() {
//        given
        RoomStay stay = RoomStay.builder()
                                .activeFrom(referenceDate.plusDays(10))
                                .activeTo(referenceDate.plusDays(15))
                                .pricePerNight(BigDecimal.valueOf(205))
                                .build();

//        when
        BigDecimal result = stay.calculateTotalCost();

//        then
        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(1025));
    }

    @ParameterizedTest
    @CsvSource({
            "PLANNED, true",
            "CANCELLED, true",
            "ACTIVE, false",
            "COMPLETED, false",
            "NOSHOW, false"
    })
    public void should_verify_can_be_cancelled(RoomStayStatus status, boolean expected) {
        RoomStay stay = RoomStay.builder().status(status).build();
        assertThat(stay.canBeCancelled()).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "PLANNED, true",
            "ACTIVE, false",
            "COMPLETED, false",
            "CANCELLED, false",
            "NOSHOW, false"
    })
    public void should_verify_can_edit_price(RoomStayStatus status, boolean expected) {
        RoomStay stay = RoomStay.builder().status(status).build();
        assertThat(stay.canEditPrice()).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "PLANNED, true",
            "ACTIVE, false",
            "COMPLETED, false",
            "CANCELLED, false",
            "NOSHOW, false"
    })
    public void should_verify_can_edit_start_date(RoomStayStatus status, boolean expected) {
        RoomStay stay = RoomStay.builder().status(status).build();
        assertThat(stay.canEditStartDate()).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "PLANNED, true",
            "ACTIVE, true",
            "COMPLETED, false",
            "CANCELLED, false",
            "NOSHOW, false"
    })
    public void should_verify_can_edit_end_date(RoomStayStatus status, boolean expected) {
        RoomStay stay = RoomStay.builder().status(status).build();
        assertThat(stay.canEditEndDate()).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "PLANNED, true",
            "ACTIVE, true",
            "COMPLETED, false",
            "CANCELLED, false",
            "NOSHOW, false"
    })
    public void should_verify_does_collide(RoomStayStatus status, boolean expected) {
        RoomStay stay = RoomStay.builder().status(status).build();
        assertThat(stay.doesCollide()).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "PLANNED, true",
            "ACTIVE, true",
            "COMPLETED, true",
            "CANCELLED, false",
            "NOSHOW, false"
    })
    public void should_verify_counts_toward_total(RoomStayStatus status, boolean expected) {
        RoomStay stay = RoomStay.builder().status(status).build();
        assertThat(stay.countsTowardTotal()).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "PLANNED, true",
            "ACTIVE, false",
            "COMPLETED, false",
            "CANCELLED, false",
            "NOSHOW, false"
    })
    public void should_verify_can_edit_room(RoomStayStatus status, boolean expected) {
        RoomStay stay = RoomStay.builder().status(status).build();
        assertThat(stay.canEditRoom()).isEqualTo(expected);
    }

    @Test
    public void should_cancel_when_allowed() {
//        given
        RoomStay stay = RoomStay.builder().status(RoomStayStatus.PLANNED).build();

//        when
        boolean result = stay.tryCancel();

//        then
        assertThat(result).isTrue();
        assertThat(stay.getStatus()).isEqualTo(RoomStayStatus.CANCELLED);
    }

    @Test
    public void should_not_cancel_when_forbidden() {
//        given
        RoomStay stay = RoomStay.builder().status(RoomStayStatus.ACTIVE).build();

//        when
        boolean result = stay.tryCancel();

//        then
        assertThat(result).isFalse();
        assertThat(stay.getStatus()).isEqualTo(RoomStayStatus.ACTIVE);
    }

    @Test
    public void should_update_active_from_when_allowed() {
//        given
        LocalDate oldDate = referenceDate;
        LocalDate newDate = referenceDate.plusDays(1);
        RoomStay stay = RoomStay.builder().status(RoomStayStatus.PLANNED).activeFrom(oldDate).build();

//        when
        boolean result = stay.tryUpdateActiveFrom(newDate);

//        then
        assertThat(result).isTrue();
        assertThat(stay.getActiveFrom()).isEqualTo(newDate);
    }

    @Test
    public void should_not_update_active_from_when_forbidden() {
//        given
        LocalDate oldDate = referenceDate;
        LocalDate newDate = referenceDate.plusDays(1);
        RoomStay stay = RoomStay.builder().status(RoomStayStatus.ACTIVE).activeFrom(oldDate).build();

//        when
        boolean result = stay.tryUpdateActiveFrom(newDate);

//        then
        assertThat(result).isFalse();
        assertThat(stay.getActiveFrom()).isEqualTo(oldDate);
    }

    @Test
    public void should_allow_updating_active_from_to_same_date_even_when_forbidden() {
//        given
        LocalDate oldDate = referenceDate;
        RoomStay stay = RoomStay.builder().status(RoomStayStatus.ACTIVE).activeFrom(oldDate).build();

//        when
        boolean result = stay.tryUpdateActiveFrom(oldDate);

//        then
        assertThat(result).isTrue();
    }

    @Test
    public void should_update_active_to_when_allowed() {
//        given
        LocalDate oldDate = referenceDate;
        LocalDate newDate = referenceDate.plusDays(1);
        RoomStay stay = RoomStay.builder().status(RoomStayStatus.ACTIVE).activeTo(oldDate).build();

//        when
        boolean result = stay.tryUpdateActiveTo(newDate);

//        then
        assertThat(result).isTrue();
        assertThat(stay.getActiveTo()).isEqualTo(newDate);
    }

    @Test
    public void should_not_update_active_to_when_forbidden() {
//        given
        LocalDate oldDate = referenceDate;
        LocalDate newDate = referenceDate.plusDays(1);
        RoomStay stay = RoomStay.builder().status(RoomStayStatus.COMPLETED).activeTo(oldDate).build();

//        when
        boolean result = stay.tryUpdateActiveTo(newDate);

//        then
        assertThat(result).isFalse();
        assertThat(stay.getActiveTo()).isEqualTo(oldDate);
    }

    @Test
    public void should_update_price_when_allowed() {
//        given
        BigDecimal oldPrice = BigDecimal.valueOf(100);
        BigDecimal newPrice = BigDecimal.valueOf(150);
        RoomStay stay = RoomStay.builder().status(RoomStayStatus.PLANNED).pricePerNight(oldPrice).build();

//        when
        boolean result = stay.tryEditPrice(newPrice);

//        then
        assertThat(result).isTrue();
        assertThat(stay.getPricePerNight()).isEqualTo(newPrice);
    }

    @Test
    public void should_not_update_price_when_forbidden() {
//        given
        BigDecimal oldPrice = BigDecimal.valueOf(100);
        BigDecimal newPrice = BigDecimal.valueOf(150);
        RoomStay stay = RoomStay.builder().status(RoomStayStatus.ACTIVE).pricePerNight(oldPrice).build();

//        when
        boolean result = stay.tryEditPrice(newPrice);

//        then
        assertThat(result).isFalse();
        assertThat(stay.getPricePerNight()).isEqualTo(oldPrice);
    }

    @Test
    public void should_return_true_when_updating_price_to_null() {
//        given
        BigDecimal oldPrice = BigDecimal.valueOf(100);
        RoomStay stay = RoomStay.builder().status(RoomStayStatus.PLANNED).pricePerNight(oldPrice).build();

//        when
        boolean result = stay.tryEditPrice(null);

//        then
        assertThat(result).isTrue();
        assertThat(stay.getPricePerNight()).isEqualTo(oldPrice);
    }

    @Test
    public void should_update_room_when_allowed() {
//        given
        Room oldRoom = Room.builder().id(1L).build();
        Room newRoom = Room.builder().id(2L).build();
        RoomStay stay = RoomStay.builder().status(RoomStayStatus.PLANNED).room(oldRoom).build();

//        when
        boolean result = stay.tryUpdateRoom(newRoom);

//        then
        assertThat(result).isTrue();
        assertThat(stay.getRoom().getId()).isEqualTo(2L);
    }

    @Test
    public void should_not_update_room_when_forbidden() {
//        given
        Room oldRoom = Room.builder().id(1L).build();
        Room newRoom = Room.builder().id(2L).build();
        RoomStay stay = RoomStay.builder().status(RoomStayStatus.ACTIVE).room(oldRoom).build();

//        when
        boolean result = stay.tryUpdateRoom(newRoom);

//        then
        assertThat(result).isFalse();
        assertThat(stay.getRoom().getId()).isEqualTo(1L);
    }

    @Test
    public void should_allow_updating_room_to_same_id_even_when_forbidden() {
//        given
        Room oldRoom = Room.builder().id(1L).build();
        Room identicalRoom = Room.builder().id(1L).build();
        RoomStay stay = RoomStay.builder().status(RoomStayStatus.ACTIVE).room(oldRoom).build();

//        when
        boolean result = stay.tryUpdateRoom(identicalRoom);

//        then
        assertThat(result).isTrue();
    }
}