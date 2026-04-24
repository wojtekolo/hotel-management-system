package io.github.wojtekolo.hotelsystem.booking.service.occupancy;

import io.github.wojtekolo.hotelsystem.booking.api.response.RoomOccupancyResponse;
import io.github.wojtekolo.hotelsystem.booking.model.entity.RoomStayStatus;
import io.github.wojtekolo.hotelsystem.booking.persistence.RoomStayRepository;
import io.github.wojtekolo.hotelsystem.common.exceptions.ResourceNotFoundException;
import io.github.wojtekolo.hotelsystem.room.persistence.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoomOccupancyCacheService {
    private final RoomStayRepository roomStayRepository;
    private final RoomRepository roomRepository;
    private final CacheManager cacheManager;
    public static final String CACHE_NAME = "occupancy";

    @Cacheable(value = CACHE_NAME, key = "#roomId")
    public RoomOccupancyResponse getAllRoomOccupancy(Long roomId, LocalDate from, LocalDate to) {
        if (!roomRepository.existsById(roomId)) throw new ResourceNotFoundException("Room not found ID: " + roomId);
        return new RoomOccupancyResponse(
                roomId,
                from,
                to,
                roomStayRepository.getOccupiedRangesForRoom(roomId,
                        RoomStayStatus.COLLIDING_STATUSES, from, to)
        );
    }

    public void evictRooms(Set<Long> ids){
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            ids.forEach(cache::evict);
        }
    }
}
