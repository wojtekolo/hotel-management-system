package io.github.wojtekolo.hotelsystem.booking.service.occupancy;

import io.github.wojtekolo.hotelsystem.booking.service.event.RoomsOccupancyChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class CacheInvalidationListener {
    private final RoomOccupancyCacheService cacheService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRoomsOccupancyChange(RoomsOccupancyChangedEvent event){
        cacheService.evictRooms(event.roomIds());
    }
}
