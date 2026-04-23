package io.github.wojtekolo.hotelsystem.booking.service.occupancy;

import io.github.wojtekolo.hotelsystem.booking.api.response.OccupiedRange;
import io.github.wojtekolo.hotelsystem.booking.api.response.RoomOccupancyResponse;
import io.github.wojtekolo.hotelsystem.booking.config.BookingProperties;
import io.github.wojtekolo.hotelsystem.booking.exception.InvalidBookingDateException;
import io.github.wojtekolo.hotelsystem.booking.exception.details.RoomOccupancyErrorCode;
import io.github.wojtekolo.hotelsystem.booking.exception.details.RoomOccupancyErrorDetails;
import io.github.wojtekolo.hotelsystem.room.api.OccupancyQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomOccupancyServiceTest {
    private RoomOccupancyService occupancyService;
    
    @Mock
    private RoomOccupancyCacheService cacheService;

    BookingProperties properties;

    private LocalDate maxDate;

    private final LocalDate today = LocalDate.now();

    @BeforeEach
    public void setup() {
        properties = new BookingProperties();
        maxDate = today.plusDays(properties.getMaxDays());
        occupancyService = new RoomOccupancyService(cacheService, properties);
    }
    
    @Test
    public void should_return_proper_ranges(){
//        given
        LocalDate searchFrom = today.plusDays(10);
        LocalDate searchTo = today.plusDays(30);

        var occupiedBefore = new OccupiedRange(today.plusDays(2), today.plusDays(6), 1L);
        var occupiedPartially = new OccupiedRange(today.plusDays(8), today.plusDays(12), 2L);
        var occupiedDuring = new OccupiedRange(today.plusDays(15), today.plusDays(20), 3L);
        var occupiedAfter = new OccupiedRange(today.plusDays(30), today.plusDays(35), 4L);
        var excludedBooking = new OccupiedRange(today.plusDays(13), today.plusDays(14), 5L);

        var cacheResponse = new RoomOccupancyResponse(1L, today, maxDate, List.of(
                occupiedBefore,
                occupiedPartially,
                occupiedDuring,
                occupiedAfter,
                excludedBooking
        ));

        when(cacheService.getAllRoomOccupancy(1L, today, maxDate)).thenReturn(cacheResponse);

        var externalQuery = new OccupancyQuery(searchFrom, searchTo, 5L);

//        when
        RoomOccupancyResponse result = occupancyService.getRoomOccupancy(1L, externalQuery);

//        then
        assertThat(result.occupiedRanges()).isNotNull();
        assertThat(result.occupiedRanges()).hasSize(2);
        assertThat(result.occupiedRanges()).extracting(OccupiedRange::from, OccupiedRange::to)
                .containsExactlyInAnyOrder(
                        tuple(occupiedPartially.from(), occupiedPartially.to()),
                        tuple(occupiedDuring.from(), occupiedDuring.to())
                );
    }

    @Test
    public void should_throw_exception_when_start_in_the_past(){
//        given
        LocalDate searchFrom = today.minusDays(5);
        LocalDate searchTo = today.plusDays(5);

        var externalQuery = new OccupancyQuery(searchFrom, searchTo, 5L);

//        when and then
        assertThatThrownBy(() -> occupancyService.getRoomOccupancy(1L, externalQuery))
                .isInstanceOf(InvalidBookingDateException.class)
                .satisfies(ex -> {
                    InvalidBookingDateException e = (InvalidBookingDateException) ex;
                    verifyException(e, RoomOccupancyErrorCode.CANNOT_CHECK_IN_THE_PAST);
                });
    }

    @Test
    public void should_throw_exception_when_invalid_order(){
//        given
        LocalDate searchFrom = today.plusDays(10);
        LocalDate searchTo = today.plusDays(5);

        var externalQuery = new OccupancyQuery(searchFrom, searchTo, 5L);

//        when and then
        assertThatThrownBy(() -> occupancyService.getRoomOccupancy(1L, externalQuery))
                .isInstanceOf(InvalidBookingDateException.class)
                .satisfies(ex -> {
                    InvalidBookingDateException e = (InvalidBookingDateException) ex;
                    verifyException(e, RoomOccupancyErrorCode.START_DATE_AFTER_END_DATE);
                });
    }

    @Test
    public void should_throw_exception_when_end_exceeds_limit(){
//        given
        LocalDate searchFrom = today.plusDays(10);
        LocalDate searchTo = maxDate.plusDays(1);

        var externalQuery = new OccupancyQuery(searchFrom, searchTo, 5L);

//        when and then
        assertThatThrownBy(() -> occupancyService.getRoomOccupancy(1L, externalQuery))
                .isInstanceOf(InvalidBookingDateException.class)
                .satisfies(ex -> {
                    InvalidBookingDateException e = (InvalidBookingDateException) ex;
                    verifyException(e, RoomOccupancyErrorCode.DATE_AFTER_LIMIT);
                });
    }

    private void verifyException(InvalidBookingDateException ex, RoomOccupancyErrorCode errorCode){
        assertThat(ex.getDetails())
                .extracting(RoomOccupancyErrorDetails::errorCode)
                .contains(errorCode);
    }


}