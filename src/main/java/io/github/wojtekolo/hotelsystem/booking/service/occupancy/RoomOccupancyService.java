package io.github.wojtekolo.hotelsystem.booking.service.occupancy;

import io.github.wojtekolo.hotelsystem.booking.api.response.OccupiedRange;
import io.github.wojtekolo.hotelsystem.booking.api.response.RoomOccupancyResponse;
import io.github.wojtekolo.hotelsystem.booking.config.BookingProperties;
import io.github.wojtekolo.hotelsystem.booking.exception.InvalidBookingDateException;
import io.github.wojtekolo.hotelsystem.booking.exception.details.RoomOccupancyErrorCode;
import io.github.wojtekolo.hotelsystem.booking.exception.details.RoomOccupancyErrorDetails;
import io.github.wojtekolo.hotelsystem.room.api.OccupancyQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RoomOccupancyService {
    private final RoomOccupancyCacheService cacheService;
    private final BookingProperties bookingProperties;

    public RoomOccupancyResponse getRoomOccupancy(Long roomId, OccupancyQuery q) {
        LocalDate today = LocalDate.now();
        LocalDate maxDate = today.plusDays(bookingProperties.getMaxDays());
        validateQuery(q, today, maxDate);

        RoomOccupancyResponse allOccupancy = cacheService
                .getAllRoomOccupancy(roomId, today, maxDate);

        List<OccupiedRange> relevantOccupancy = allOccupancy.occupiedRanges().stream()
                .filter(range -> !Objects.equals(range.bookingId(), q.excludeBookingId()))
                .filter(range -> range.from().isBefore(q.to()) && range.to().isAfter(q.from()))
                .toList();

        return new RoomOccupancyResponse(roomId, q.from(), q.to(), relevantOccupancy);
    }

    private void validateQuery(OccupancyQuery q, LocalDate today, LocalDate maxDate){
        List<RoomOccupancyErrorDetails> details = new ArrayList<>();
        if (q.from().isBefore(today)) {
            details.add(new RoomOccupancyErrorDetails(q.from(),
                    RoomOccupancyErrorCode.CANNOT_CHECK_IN_THE_PAST));
        }

        if (q.from().isAfter(q.to())){
            details.add(new RoomOccupancyErrorDetails(q.from(),
                    RoomOccupancyErrorCode.START_DATE_AFTER_END_DATE));
        }

        if (q.from().isAfter(maxDate)){
            details.add(new RoomOccupancyErrorDetails(q.from(),
                    RoomOccupancyErrorCode.DATE_AFTER_LIMIT));
        }

        if (q.to().isAfter(maxDate)){
            details.add(new RoomOccupancyErrorDetails(q.to(),
                    RoomOccupancyErrorCode.DATE_AFTER_LIMIT));
        }

        if (!details.isEmpty())
            throw new InvalidBookingDateException(details, "Error getting room occupation");
    }
}
