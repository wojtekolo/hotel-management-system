package io.github.wojtekolo.hotelsystem.booking.service;

import io.github.wojtekolo.hotelsystem.booking.BookingTestUtils;
import io.github.wojtekolo.hotelsystem.booking.api.request.BookingCreateRequest;
import io.github.wojtekolo.hotelsystem.booking.api.request.BookingUpdateRequest;
import io.github.wojtekolo.hotelsystem.booking.model.entity.Booking;
import io.github.wojtekolo.hotelsystem.booking.service.loading.BookingRequestHelper;
import io.github.wojtekolo.hotelsystem.room.RoomTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class BookingRequestHelperTest {

    private BookingRequestHelper requestHelper;

    @BeforeEach
    void setUp() {
        requestHelper = new BookingRequestHelper();
    }

    @Test
    public void should_return_all_ids_when_only_request() {
//        given
        BookingCreateRequest request = new BookingCreateRequest(0L, 0L, List.of(
                BookingTestUtils.createRoomStayCreateRequest(1L),
                BookingTestUtils.createRoomStayCreateRequest(2L),
                BookingTestUtils.createRoomStayCreateRequest(3L),
                BookingTestUtils.createRoomStayCreateRequest(2L),
                BookingTestUtils.createRoomStayCreateRequest(4L)
        ));

//        when
        Set<Long> result = requestHelper.collectAllRoomIds(request);

//        then
        assertThat(result).containsExactlyInAnyOrder(1L, 2L, 3L, 4L);
    }

    @Test
    public void should_return_all_ids_when_request_and_booking() {
//        given
        BookingUpdateRequest request = new BookingUpdateRequest(0L, List.of(
                BookingTestUtils.createRoomStayUpdateRequest(1L),
                BookingTestUtils.createRoomStayUpdateRequest(2L),
                BookingTestUtils.createRoomStayUpdateRequest(3L),
                BookingTestUtils.createRoomStayUpdateRequest(2L),
                BookingTestUtils.createRoomStayUpdateRequest(4L)
        ));

        Booking booking = BookingTestUtils.aValidBooking()
                .stays(
                        List.of(
                                BookingTestUtils.aValidRoomStay().room(RoomTestUtils.aValidRoom().id(1L).build()).build(),
                                BookingTestUtils.aValidRoomStay().room(RoomTestUtils.aValidRoom().id(3L).build()).build(),
                                BookingTestUtils.aValidRoomStay().room(RoomTestUtils.aValidRoom().id(7L).build()).build()
                        )).build();

//        when
        Set<Long> result = requestHelper.collectAllRoomIds(booking, request);

//        then
        assertThat(result).containsExactlyInAnyOrder(1L, 2L, 3L, 4L, 7L);
    }
}