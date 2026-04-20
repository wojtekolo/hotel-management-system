package io.github.wojtekolo.hotelsystem.booking.service.availability;

import io.github.wojtekolo.hotelsystem.booking.api.response.OccupiedRange;
import io.github.wojtekolo.hotelsystem.booking.api.response.RoomOccupancyResponse;
import io.github.wojtekolo.hotelsystem.booking.model.entity.RoomStayStatus;
import io.github.wojtekolo.hotelsystem.booking.persistence.RoomStayRepository;
import io.github.wojtekolo.hotelsystem.room.api.OccupancyQuery;
import io.github.wojtekolo.hotelsystem.room.persistence.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomAvailabilityServiceTest {
    private RoomAvailabilityService availabilityService;
    @Mock
    private RoomStayRepository roomStayRepository;

    @Mock
    private RoomRepository roomRepository;

    @BeforeEach
    public void setup() {
        availabilityService = new RoomAvailabilityService(roomStayRepository, roomRepository);
    }

    @Test
    public void should_return_proper_response() {
//        given
        LocalDate today = LocalDate.now();
        when(roomRepository.existsById(1L)).thenReturn(true);

        when(roomStayRepository.getOccupiedRangesForRoom(1L, RoomStayStatus.COLLIDING_STATUSES,
                today, today.plusDays(20), null))
                .thenReturn(List.of(
                        new OccupiedRange(today.plusDays(3), today.plusDays(8), 1L),
                        new OccupiedRange(today.plusDays(15), today.plusDays(22), 2L)
                ));

//        when
        RoomOccupancyResponse response = availabilityService.getRoomOccupancy(1L, new OccupancyQuery(
                        today,
                        today.plusDays(20),
                        null
                )
        );

//        then
        assertThat(response.roomId()).isEqualTo(1L);
        assertThat(response.searchFrom()).isEqualTo(today);
        assertThat(response.searchTo()).isEqualTo(today.plusDays(20));
        assertThat(response.occupiedRanges())
                .extracting(OccupiedRange::from, OccupiedRange::to, OccupiedRange::bookingId)
                .containsExactlyInAnyOrder(
                        tuple(
                                today.plusDays(3),
                                today.plusDays(8),
                                1L
                        ),
                        tuple(
                                today.plusDays(15),
                                today.plusDays(22),
                                2L
                        )
                );
    }
}