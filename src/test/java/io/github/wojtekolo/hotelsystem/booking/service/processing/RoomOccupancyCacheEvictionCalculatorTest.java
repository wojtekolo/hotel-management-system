package io.github.wojtekolo.hotelsystem.booking.service.processing;

import io.github.wojtekolo.hotelsystem.booking.api.request.RoomStayUpdateRequest;
import io.github.wojtekolo.hotelsystem.booking.model.entity.Booking;
import io.github.wojtekolo.hotelsystem.booking.model.entity.RoomStay;
import io.github.wojtekolo.hotelsystem.booking.model.entity.RoomStayStatus;
import io.github.wojtekolo.hotelsystem.room.model.Room;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


class RoomOccupancyCacheEvictionCalculatorTest {

    private final RoomOccupancyCacheEvictionCalculator calculator = new RoomOccupancyCacheEvictionCalculator();
    private final LocalDate today = LocalDate.now();

    @Test
    void shouldReturnDeletedRoom() {
//        given
        var room = Room.builder()
                       .id(1L)
                       .build();

        var stay = RoomStay.builder()
                           .id(10L)
                           .room(room)
                           .status(RoomStayStatus.PLANNED)
                           .build();

        var booking = Booking.builder()
                             .stays(List.of(stay))
                             .build();
//        when
        Set<Long> result = calculator.calculateAffectedRoomIds(booking, List.of());

//        then
        assertThat(result).containsExactly(1L);
    }

    @Test
    void should_return_room_when_updating_active_from() {
//        given
        var room = Room.builder()
                       .id(1L)
                       .build();

        var stay = RoomStay.builder()
                           .id(10L)
                           .room(room)
                           .activeFrom(today)
                           .activeTo(today.plusDays(10))
                           .status(RoomStayStatus.PLANNED)
                           .build();

        var booking = Booking.builder()
                             .stays(List.of(stay))
                             .build();

        var request = new RoomStayUpdateRequest(
                10L,
                1L,
                today.plusDays(1),
                today.plusDays(10),
                null
        );
//        when
        Set<Long> result = calculator.calculateAffectedRoomIds(booking, List.of(request));

//        then
        assertThat(result).containsExactly(1L);
    }

    @Test
    void should_return_room_when_updating_active_to() {
//        given
        var room = Room.builder()
                       .id(1L)
                       .build();

        var stay = RoomStay.builder()
                           .id(10L)
                           .room(room)
                           .activeFrom(today)
                           .activeTo(today.plusDays(10))
                           .status(RoomStayStatus.PLANNED)
                           .build();

        var booking = Booking.builder()
                             .stays(List.of(stay))
                             .build();

        var request = new RoomStayUpdateRequest(
                10L,
                1L,
                today,
                today.plusDays(11),
                null
        );
//        when
        Set<Long> result = calculator.calculateAffectedRoomIds(booking, List.of(request));

//        then
        assertThat(result).containsExactly(1L);
    }

    @Test
    void should_return_two_rooms_when_updating_room() {
//        given
        var room = Room.builder()
                       .id(1L)
                       .build();

        var stay = RoomStay.builder()
                           .id(10L)
                           .room(room)
                           .activeFrom(today)
                           .activeTo(today.plusDays(10))
                           .status(RoomStayStatus.PLANNED)
                           .build();

        var booking = Booking.builder()
                             .stays(List.of(stay))
                             .build();

        var request = new RoomStayUpdateRequest(
                10L,
                2L,
                today,
                today.plusDays(11),
                null
        );
//        when
        Set<Long> result = calculator.calculateAffectedRoomIds(booking, List.of(request));

//        then
        assertThat(result).containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void should_return_room_when_adding_new_stay() {
//        given
        var room1 = Room.builder()
                       .id(1L)
                       .build();

        var stay = RoomStay.builder()
                           .id(10L)
                           .room(room1)
                           .activeFrom(today)
                           .activeTo(today.plusDays(10))
                           .status(RoomStayStatus.PLANNED)
                           .build();

        var booking = Booking.builder()
                             .stays(List.of(stay))
                             .build();

        var request1 = new RoomStayUpdateRequest(
                10L,
                1L,
                today,
                today.plusDays(10),
                null
        );

        var request2 = new RoomStayUpdateRequest(
                null,
                2L,
                today,
                today.plusDays(10),
                null
        );
//        when
        Set<Long> result = calculator.calculateAffectedRoomIds(booking, List.of(request1, request2));

//        then
        assertThat(result).containsExactly(2L);
    }

    @Test
    void should_return_empty_when_not_changed() {
//        given
        var room = Room.builder()
                       .id(1L)
                       .build();

        var stay = RoomStay.builder()
                           .id(10L)
                           .room(room)
                           .activeFrom(today)
                           .activeTo(today.plusDays(10))
                           .status(RoomStayStatus.PLANNED)
                           .build();

        var booking = Booking.builder()
                             .stays(List.of(stay))
                             .build();

        var request = new RoomStayUpdateRequest(
                10L,
                1L,
                today,
                today.plusDays(10),
                null
        );
//        when
        Set<Long> result = calculator.calculateAffectedRoomIds(booking, List.of(request));

//        then
        assertThat(result).isEmpty();
    }

}