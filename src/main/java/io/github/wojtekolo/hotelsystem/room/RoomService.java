package io.github.wojtekolo.hotelsystem.room;

import io.github.wojtekolo.hotelsystem.room.dtos.RoomListItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
public class RoomService {
    private RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public Slice<RoomListItem> getRooms(Pageable pageable) {
        return roomRepository.findAllRooms(pageable);
    }
}
