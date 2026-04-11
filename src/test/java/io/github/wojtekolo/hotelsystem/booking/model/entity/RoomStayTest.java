package io.github.wojtekolo.hotelsystem.booking.model.entity;

import io.github.wojtekolo.hotelsystem.booking.model.violations.RoomStayViolationReason;
import io.github.wojtekolo.hotelsystem.room.RoomTestUtils;
import io.github.wojtekolo.hotelsystem.room.model.Room;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

class RoomStayTest {
    private final LocalDate referenceDate = LocalDate.now();

    @Nested
    @DisplayName("Update operations")
    class UpdateOperations {
        @Nested
        @DisplayName("Cancel")
        class Cancel {
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

            @Test
            public void should_return_empty_when_can_be_canceled() {
//                  given

                RoomStay roomStay = RoomStay.builder().status(RoomStayStatus.PLANNED).build();
//                  when
                Optional<RoomStayViolationReason> result = roomStay.tryCancel();

//                  then
                assertThat(result).isEmpty();
                assertThat(roomStay.getStatus()).isEqualTo(RoomStayStatus.CANCELLED);
            }

            @Test
            public void should_return_empty_when_cannot_be_canceled_but_already_canceled() {
//                  given
                RoomStay roomStay = RoomStay.builder().status(RoomStayStatus.CANCELLED).build();
//                  when
                Optional<RoomStayViolationReason> result = roomStay.tryCancel();

//                  then
                assertThat(result).isEmpty();
            }

            @Test
            public void should_return_violation_when_cannot_be_canceled() {
//                  given
                RoomStay roomStay = RoomStay.builder().status(RoomStayStatus.ACTIVE).build();
//                  when
                Optional<RoomStayViolationReason> result = roomStay.tryCancel();

//                  then
                assertThat(result).isPresent();
                assertThat(result.get()).isEqualTo(RoomStayViolationReason.CANCEL_INVALID_STATUS);
                assertThat(roomStay.getStatus()).isEqualTo(RoomStayStatus.ACTIVE);
            }
        }

        @Nested
        @DisplayName("Update price")
        class UpdatePrice {
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

            @Test
            public void should_return_empty_when_can_update_price() {
//                  given
                RoomStay roomStay = RoomStay.builder().status(RoomStayStatus.PLANNED).pricePerNight(BigDecimal.ONE)
                                            .build();
//                  when
                Optional<RoomStayViolationReason> result = roomStay.tryUpdatePrice(BigDecimal.TWO);

//                  then
                assertThat(result).isEmpty();
                assertThat(roomStay.getPricePerNight()).isEqualTo(BigDecimal.TWO);
            }

            @Test
            public void should_return_empty_when_cannot_update_price_but_the_same_value() {
//                  given
                RoomStay roomStay = RoomStay.builder().status(RoomStayStatus.COMPLETED).pricePerNight(BigDecimal.ONE)
                                            .build();
//                  when
                Optional<RoomStayViolationReason> result = roomStay.tryUpdatePrice(BigDecimal.ONE);

//                  then
                assertThat(result).isEmpty();
            }

            @Test
            public void should_return_violation_when_cannot_update_price() {
//                  given
                RoomStay roomStay = RoomStay.builder().status(RoomStayStatus.COMPLETED).pricePerNight(BigDecimal.ONE)
                                            .build();
//                  when
                Optional<RoomStayViolationReason> result = roomStay.tryUpdatePrice(BigDecimal.TWO);

//                  then
                assertThat(result).isPresent();
                assertThat(result.get()).isEqualTo(RoomStayViolationReason.PRICE_EDIT_INVALID_STATUS);
                assertThat(roomStay.getPricePerNight()).isEqualTo(BigDecimal.ONE);
            }

            @Test
            public void should_return_empty_when_null_price(){
//                  given
                RoomStay roomStay = RoomStay.builder().status(RoomStayStatus.PLANNED).pricePerNight(BigDecimal.ONE)
                                            .build();
//                  when
                Optional<RoomStayViolationReason> result = roomStay.tryUpdatePrice(null);

//                  then
                assertThat(result).isEmpty();
            }
        }

        @Nested
        @DisplayName("Update active to")
        class updateActiveTo {

            @ParameterizedTest
            @CsvSource({
                    "PLANNED, true",
                    "ACTIVE, true",
                    "COMPLETED, false",
                    "CANCELLED, false",
                    "NOSHOW, false"
            })
            public void should_verify_can_edit_active_to(RoomStayStatus status, boolean expected) {
                RoomStay stay = RoomStay.builder().status(status).build();
                assertThat(stay.canEditEndDate()).isEqualTo(expected);
            }

            @Test
            public void should_return_empty_when_can_update_active_to() {
//                  given
                RoomStay roomStay = RoomStay.builder()
                                            .activeFrom(referenceDate.plusDays(5))
                                            .activeTo(referenceDate.plusDays(10))
                                            .status(RoomStayStatus.PLANNED).build();
//                  when
                Optional<RoomStayViolationReason> result = roomStay.tryUpdateActiveTo(LocalDate.now().plusDays(11));

//                  then
                assertThat(result).isEmpty();
                assertThat(roomStay.getActiveTo()).isEqualTo(LocalDate.now().plusDays(11));
            }

            @Test
            public void should_return_empty_when_cannot_update_active_to_but_the_same_day() {
//                  given
                RoomStay roomStay = RoomStay.builder()
                                            .activeFrom(referenceDate.plusDays(5))
                                            .activeTo(referenceDate.plusDays(10))
                                            .status(RoomStayStatus.PLANNED).build();
//                  when
                Optional<RoomStayViolationReason> result = roomStay.tryUpdateActiveTo(referenceDate.plusDays(10));

//                  then
                assertThat(result).isEmpty();
            }

            @Test
            public void should_return_violation_when_cannot_update_active_to() {
//                  given
                RoomStay roomStay = RoomStay.builder()
                                            .activeFrom(LocalDate.now().plusDays(5))
                                            .activeTo(LocalDate.now().plusDays(10))
                                            .status(RoomStayStatus.COMPLETED).build();
//                  when
                Optional<RoomStayViolationReason> result = roomStay.tryUpdateActiveTo(LocalDate.now().plusDays(11));

//                  then
                assertThat(result).isPresent();
                assertThat(result.get()).isEqualTo(RoomStayViolationReason.END_DATE_EDIT_INVALID_STATUS);
                assertThat(roomStay.getActiveTo()).isEqualTo(LocalDate.now().plusDays(10));
            }

//            @Test
//            public void should_return_violation_when_new_active_to_is_not_after_active_from() {
////                  given
//                RoomStay roomStay = RoomStay.builder()
//                                            .activeFrom(referenceDate.plusDays(5))
//                                            .activeTo(referenceDate.plusDays(10))
//                                            .status(RoomStayStatus.PLANNED).build();
////                  when
//                Optional<RoomStayViolationReason> result = roomStay.tryUpdateActiveTo(referenceDate.plusDays(5));
//
////                  then
//                assertThat(result).isPresent();
//                assertThat(result.get()).isEqualTo(RoomStayViolationReason.END_DATE_NOT_AFTER_START_DATE);
//                assertThat(roomStay.getActiveTo()).isEqualTo(referenceDate.plusDays(10));
//            }
        }

        @Nested
        @DisplayName("Update active from")
        class updateActiveFrom {
            @ParameterizedTest
            @CsvSource({
                    "PLANNED, true",
                    "ACTIVE, false",
                    "COMPLETED, false",
                    "CANCELLED, false",
                    "NOSHOW, false"
            })
            public void should_verify_can_edit_active_from(RoomStayStatus status, boolean expected) {
                RoomStay stay = RoomStay.builder().status(status).build();
                assertThat(stay.canEditStartDate()).isEqualTo(expected);
            }

            @Test
            public void should_return_empty_when_can_update_active_from() {
//                  given
                RoomStay roomStay = RoomStay.builder()
                                            .activeFrom(referenceDate.plusDays(5))
                                            .activeTo(referenceDate.plusDays(10))
                                            .status(RoomStayStatus.PLANNED).build();
//                  when
                Optional<RoomStayViolationReason> result = roomStay.tryUpdateActiveFrom(LocalDate.now().plusDays(6));

//                  then
                assertThat(result).isEmpty();
                assertThat(roomStay.getActiveFrom()).isEqualTo(LocalDate.now().plusDays(6));
            }

            @Test
            public void should_return_empty_when_cannot_update_active_from_but_the_same_day() {
//                  given
                RoomStay roomStay = RoomStay.builder()
                                            .activeFrom(referenceDate.plusDays(5))
                                            .activeTo(referenceDate.plusDays(10))
                                            .status(RoomStayStatus.ACTIVE).build();
//                  when
                Optional<RoomStayViolationReason> result = roomStay.tryUpdateActiveFrom(referenceDate.plusDays(5));

//                  then
                assertThat(result).isEmpty();
            }

            @Test
            public void should_return_violation_when_cannot_update_active_from() {
//                  given
                RoomStay roomStay = RoomStay.builder().status(RoomStayStatus.ACTIVE)
                                            .activeFrom(referenceDate.plusDays(5))
                                            .activeTo(referenceDate.plusDays(10))
                                            .build();
//                  when
                Optional<RoomStayViolationReason> result = roomStay.tryUpdateActiveFrom(LocalDate.now().plusDays(6));

//                  then
                assertThat(result).isPresent();
                assertThat(result.get()).isEqualTo(RoomStayViolationReason.START_DATE_EDIT_INVALID_STATUS);
                assertThat(roomStay.getActiveFrom()).isEqualTo(referenceDate.plusDays(5));
            }

//            @Test
//            public void should_return_violation_when_new_active_from_is_in_the_past() {
////                  given
//                RoomStay roomStay = RoomStay.builder()
//                                            .activeFrom(referenceDate.plusDays(5))
//                                            .activeTo(referenceDate.plusDays(10))
//                                            .status(RoomStayStatus.PLANNED).build();
////                  when
//                Optional<RoomStayViolationReason> result = roomStay.tryUpdateActiveFrom(LocalDate.now().minusDays(1));
//
////                  then
//                assertThat(result).isPresent();
//                assertThat(result.get()).isEqualTo(RoomStayViolationReason.START_DATE_IN_THE_PAST);
//                assertThat(roomStay.getActiveFrom()).isEqualTo(referenceDate.plusDays(5));
//            }
//
//            @Test
//            public void should_return_violation_when_new_active_from_is_not_before_active_to() {
////                  given
//                RoomStay roomStay = RoomStay.builder()
//                                            .activeFrom(referenceDate.plusDays(5))
//                                            .activeTo(referenceDate.plusDays(10))
//                                            .status(RoomStayStatus.PLANNED).build();
////                  when
//                Optional<RoomStayViolationReason> result = roomStay.tryUpdateActiveFrom(referenceDate.plusDays(10));
//
////                  then
//                assertThat(result).isPresent();
//                assertThat(result.get()).isEqualTo(RoomStayViolationReason.END_DATE_NOT_AFTER_START_DATE);
//                assertThat(roomStay.getActiveFrom()).isEqualTo(referenceDate.plusDays(5));
//            }
        }

        @Nested
        @DisplayName("Update room")
        class updateRoom {
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
            public void should_return_empty_when_can_update_room() {
//                  given
                RoomStay roomStay = RoomStay.builder().status(RoomStayStatus.PLANNED).build();
                Room newRoom = RoomTestUtils.aValidRoom().build();
//                  when
                Optional<RoomStayViolationReason> result = roomStay.tryUpdateRoom(newRoom);

//                  then
                assertThat(result).isEmpty();
                assertThat(roomStay.getRoom()).isEqualTo(newRoom);
            }

            @Test
            public void should_return_empty_when_cannot_update_room_but_the_same_room() {
//                  given
                Room room = RoomTestUtils.aValidRoom().id(1L).build();
                RoomStay roomStay = RoomStay.builder().room(room).status(RoomStayStatus.ACTIVE).build();
//                  when
                Optional<RoomStayViolationReason> result = roomStay.tryUpdateRoom(room);

//                  then
                assertThat(result).isEmpty();
            }

            @Test
            public void should_return_violation_when_cannot_update_room() {
//                  given
                Room room = RoomTestUtils.aValidRoom().id(1L).build();
                RoomStay roomStay = RoomStay.builder().room(room).status(RoomStayStatus.ACTIVE).build();
//                  when
                Optional<RoomStayViolationReason> result = roomStay.tryUpdateRoom(RoomTestUtils.aValidRoom().id(2L)
                                                                                               .build());

//                  then
                assertThat(result).isPresent();
                assertThat(result.get()).isEqualTo(RoomStayViolationReason.ROOM_EDIT_INVALID_STATUS);
                assertThat(roomStay.getRoom().getId()).isEqualTo(1L);
            }

        }
    }

    @Nested
    @DisplayName("Price calculations")
    class PriceCalculations {
        @Test
        public void should_calculate_correct_price_when_5_days() {
//              given
            RoomStay roomStay = RoomStay.builder()
                                        .pricePerNight(BigDecimal.valueOf(120))
                                        .activeFrom(referenceDate.plusDays(5))
                                        .activeTo(referenceDate.plusDays(10))
                                        .build();
//              when
            BigDecimal result = roomStay.calculateTotalCost();

//              then
            assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(600));
        }

        @Test
        public void should_calculate_correct_price_when_single_night() {
//              given
            RoomStay roomStay = RoomStay.builder()
                                        .pricePerNight(BigDecimal.valueOf(120))
                                        .activeFrom(LocalDate.of(2026, 10, 10))
                                        .activeTo(LocalDate.of(2026, 10, 11))
                                        .build();
//              when
            BigDecimal result = roomStay.calculateTotalCost();

//              then
            assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(120));
        }

        @Test
        public void should_calculate_correct_price_when_zero_nights() {
//              given
            RoomStay roomStay = RoomStay.builder()
                                        .pricePerNight(BigDecimal.valueOf(120))
                                        .activeFrom(LocalDate.of(2026, 10, 10))
                                        .activeTo(LocalDate.of(2026, 10, 10))
                                        .build();
//              when
            BigDecimal result = roomStay.calculateTotalCost();

//              then
            assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(0));
        }

        @Test
        public void should_calculate_correct_price_when_illegal_period() {
//              given
            RoomStay roomStay = RoomStay.builder()
                                        .pricePerNight(BigDecimal.valueOf(120))
                                        .activeFrom(LocalDate.of(2026, 10, 15))
                                        .activeTo(LocalDate.of(2026, 10, 10))
                                        .build();
//              when
            BigDecimal result = roomStay.calculateTotalCost();

//              then
            assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(0));
        }

        @Test
        public void should_handle_rounding_correctly() {
//              given
//              133.33 * 0.9 = 119.997
            BigDecimal discount = new BigDecimal("0.1");
            BigDecimal roomPrice = new BigDecimal("133.33");
            BigDecimal customPrice = null;

//              when
            BigDecimal result = RoomStay.calculatePricePerNight(roomPrice, discount, customPrice);

//              then
            assertThat(result).isEqualByComparingTo(new BigDecimal("120.00"));
        }

        @Test
        public void should_apply_discount_when_custom_price_is_null() {
//              given
            BigDecimal roomPrice = BigDecimal.valueOf(500);
            BigDecimal discount = BigDecimal.valueOf(0.1);
            BigDecimal customPrice = null;
//              when
            BigDecimal result = RoomStay.calculatePricePerNight(roomPrice, discount, customPrice);

//              then
            assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(450));
        }

        @Test
        public void should_not_apply_discount_when_custom_price() {
//              given
            BigDecimal roomPrice = BigDecimal.valueOf(500);
            BigDecimal discount = BigDecimal.valueOf(0.1);
            BigDecimal customPrice = BigDecimal.valueOf(300);
//              when
            BigDecimal result = RoomStay.calculatePricePerNight(roomPrice, discount, customPrice);

//              then
            assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(300));
        }

        @Test
        public void should_fail_when_discount_is_less_than_0() {
//              given
            BigDecimal roomPrice = BigDecimal.valueOf(100);
            BigDecimal discount = BigDecimal.valueOf(-1);
            BigDecimal customPrice = BigDecimal.valueOf(300);
//              when and then
            assertThatThrownBy(() -> RoomStay.calculatePricePerNight(roomPrice, discount, customPrice))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        public void should_succeed_when_discount_is_equal_to_0() {
//              given
            BigDecimal roomPrice = BigDecimal.valueOf(100);
            BigDecimal discount = BigDecimal.ZERO;
            BigDecimal customPrice = BigDecimal.valueOf(300);
//              when
            BigDecimal result = RoomStay.calculatePricePerNight(roomPrice, discount, customPrice);

//              then
            assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(300));
        }

        @Test
        public void should_succeed_when_discount_is_equal_to_1() {
//              given
            BigDecimal roomPrice = BigDecimal.valueOf(100);
            BigDecimal discount = BigDecimal.valueOf(1);
            BigDecimal customPrice = null;
//              when
            BigDecimal result = RoomStay.calculatePricePerNight(roomPrice, discount, customPrice);

//              then
            assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        public void should_fail_when_discount_is_greater_than_1() {
//              given
            BigDecimal roomPrice = BigDecimal.valueOf(100);
            BigDecimal discount = BigDecimal.valueOf(2);
            BigDecimal customPrice = null;
//              when and then
            assertThatThrownBy(() -> RoomStay.calculatePricePerNight(roomPrice, discount, customPrice))
                    .isInstanceOf(IllegalArgumentException.class);
        }

//        @Test
//        public void should_fail_when_custom_price_is_negative() {
////              given
//            BigDecimal roomPrice = BigDecimal.valueOf(100);
//            BigDecimal discount = BigDecimal.valueOf(0.1);
//            BigDecimal customPrice = BigDecimal.valueOf(-300);
//
////              when and then
//            assertThatThrownBy(() -> RoomStay.calculatePricePerNight(roomPrice, discount, customPrice))
//                    .isInstanceOf(IllegalArgumentException.class);
//        }
    }

    @Nested
    @DisplayName("RoomStay creation")
    class StayCreation {
        @Test
        public void should_create_planned() {
//              when
            RoomStay roomStay = RoomStay.createPlanned(null, RoomTestUtils.aValidRoom()
                                                                          .build(), BigDecimal.ZERO, null, null, null, null);
//              then
            assertThat(roomStay.getStatus()).isEqualTo(RoomStayStatus.PLANNED);
        }
        @Test
        public void should_create_planned_room_stay_with_discounted_price() {
//              given
            Room room = Room.builder()
                            .type(RoomTestUtils.aValidType().pricePerNight(BigDecimal.valueOf(500)).build())
                            .build();
            BigDecimal discount = BigDecimal.valueOf(0.1);

//              when
            RoomStay roomStay = RoomStay.createPlanned(null, room, discount, null,
                    referenceDate.plusDays(5), referenceDate.plusDays(10), null);

//              then
            assertThat(roomStay.getPricePerNight()).isEqualByComparingTo(BigDecimal.valueOf(450));
            assertThat(roomStay.getStatus()).isEqualTo(RoomStayStatus.PLANNED);
        }

        @Test
        public void should_create_planned_room_stay_with_custom_price_ignoring_discount() {
//              given
            Room room = Room.builder().type(RoomTestUtils.aValidType().pricePerNight(BigDecimal.valueOf(500)).build())
                            .build();

            BigDecimal discount = BigDecimal.valueOf(0.1);
            BigDecimal customPrice = BigDecimal.valueOf(600);

//              when
            RoomStay roomStay = RoomStay.createPlanned(null, room, discount, null,
                    referenceDate.plusDays(5), referenceDate.plusDays(10), customPrice);

//              then
            assertThat(roomStay.getPricePerNight()).isEqualByComparingTo(customPrice);
        }
    }

    @Nested
    @DisplayName("Other")
    class Other{
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
    }

}