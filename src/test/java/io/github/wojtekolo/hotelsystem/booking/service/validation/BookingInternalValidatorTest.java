package io.github.wojtekolo.hotelsystem.booking.service.validation;

import io.github.wojtekolo.hotelsystem.booking.exception.details.RoomStayInternalConflict;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static io.github.wojtekolo.hotelsystem.booking.BookingTestUtils.*;
import static io.github.wojtekolo.hotelsystem.room.RoomTestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class BookingInternalValidatorTest {

    private final BookingInternalValidator internalValidator = new BookingInternalValidator();

    private final LocalDate today = LocalDate.now();

    @Test
    public void should_return_conflicts_when_there_is_overlap_between_sequential_stays_inside_request() {
//        given
        var room = aValidRoom().id(1L).name("roomname1").build();
//        when
        List<RoomStayInternalConflict> result = internalValidator.validate(
                List.of(
                        buildRoomStay(1L, room, today.plusDays(10), today.plusDays(15)),
                        buildRoomStay(2L, room, today.plusDays(13), today.plusDays(18)),
                        buildRoomStay(3L, room, today.plusDays(17), today.plusDays(20))
                )
        );

//        then
        assertThat(result)
                .extracting(
                        RoomStayInternalConflict::id1, RoomStayInternalConflict::from1, RoomStayInternalConflict::to1,
                        RoomStayInternalConflict::id2, RoomStayInternalConflict::from2, RoomStayInternalConflict::to2
                )
                .containsExactlyInAnyOrder(
                        tuple(
                                1L, today.plusDays(10), today.plusDays(15),
                                2L, today.plusDays(13), today.plusDays(18)
                        ),
                        tuple(
                                2L, today.plusDays(13), today.plusDays(18),
                                3L, today.plusDays(17), today.plusDays(20)
                        )

                );
    }

    @Test
    public void should_return_conflicts_when_one_stay_overlaps_multiple_others() {
//        given
        var room = aValidRoom().id(1L).name("roomname1").build();

//        when
        List<RoomStayInternalConflict> result = internalValidator.validate(
                List.of(
                        buildRoomStay(1L, room, today.plusDays(10), today.plusDays(30)),
                        buildRoomStay(2L, room, today.plusDays(13), today.plusDays(15)),
                        buildRoomStay(3L, room, today.plusDays(16), today.plusDays(20)),
                        buildRoomStay(4L, room, today.plusDays(22), today.plusDays(25))
                )
        );

//        then
        assertThat(result)
                .extracting(
                        RoomStayInternalConflict::id1, RoomStayInternalConflict::from1, RoomStayInternalConflict::to1,
                        RoomStayInternalConflict::id2, RoomStayInternalConflict::from2, RoomStayInternalConflict::to2
                )
                .containsExactlyInAnyOrder(
                        tuple(
                                1L, today.plusDays(10), today.plusDays(30),
                                2L, today.plusDays(13), today.plusDays(15)
                        ),
                        tuple(
                                1L, today.plusDays(10), today.plusDays(30),
                                3L, today.plusDays(16), today.plusDays(20)
                        ),
                        tuple(
                                1L, today.plusDays(10), today.plusDays(30),
                                4L, today.plusDays(22), today.plusDays(25)
                        )

                );
    }

    @Test
    public void should_return_empty_list_when_no_gap_between_sequential_stays_inside_request() {
//        given
        var room = aValidRoom().id(1L).name("roomname1").build();

//        when
        List<RoomStayInternalConflict> result = internalValidator.validate(
                List.of(
                        buildRoomStay(1L, room, today.plusDays(10), today.plusDays(15)),
                        buildRoomStay(2L, room, today.plusDays(15), today.plusDays(20)),
                        buildRoomStay(3L, room, today.plusDays(20), today.plusDays(25))
                )
        );

//        then
        assertThat(result).hasSize(0);
    }

    @Test
    public void should_return_empty_list_when_different_rooms_and_same_period() {
//        given
        var room1 = aValidRoom().id(1L).build();
        var room2 = aValidRoom().id(2L).build();

//        when
        List<RoomStayInternalConflict> result = internalValidator.validate(
                List.of(
                        buildRoomStay(1L, room1, today.plusDays(10), today.plusDays(15)),
                        buildRoomStay(2L, room2, today.plusDays(10), today.plusDays(15))
                )
        );

//        then
        assertThat(result).hasSize(0);
    }
}