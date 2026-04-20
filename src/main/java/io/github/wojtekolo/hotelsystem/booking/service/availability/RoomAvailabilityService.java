package io.github.wojtekolo.hotelsystem.booking.service.availability;

import io.github.wojtekolo.hotelsystem.booking.api.response.RoomOccupancyResponse;
import io.github.wojtekolo.hotelsystem.booking.model.entity.RoomStayStatus;
import io.github.wojtekolo.hotelsystem.booking.persistence.RoomStayRepository;
import io.github.wojtekolo.hotelsystem.common.exceptions.ResourceNotFoundException;
import io.github.wojtekolo.hotelsystem.room.persistence.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class RoomAvailabilityService {
    private final RoomStayRepository roomStayRepository;
    private final RoomRepository roomRepository;

    public RoomOccupancyResponse getRoomOccupancy(Long roomId, LocalDate from, LocalDate to){
        if (!roomRepository.existsById(roomId)) throw new ResourceNotFoundException("Room not found ID: "+roomId);
        return new RoomOccupancyResponse(roomId, from, to,
                roomStayRepository.getOccupiedRangesForRoom(roomId, RoomStayStatus.COLLIDING_STATUSES, from, to));
    }
}
