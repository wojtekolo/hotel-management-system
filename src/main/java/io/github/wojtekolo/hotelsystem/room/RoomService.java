package io.github.wojtekolo.hotelsystem.room;

import io.github.wojtekolo.hotelsystem.common.exceptions.ResourceAlreadyExistsException;
import io.github.wojtekolo.hotelsystem.common.exceptions.ResourceNotFoundException;
import io.github.wojtekolo.hotelsystem.room.dtos.RoomCreateRequest;
import io.github.wojtekolo.hotelsystem.room.dtos.RoomListItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
public class RoomService {
    private final RoomRepository roomRepository;
    private final RoomStatusRepository roomStatusRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final RoomMapper roomMapper;

    public RoomService(
            RoomRepository roomRepository,
            RoomStatusRepository roomStatusRepository,
            RoomTypeRepository roomTypeRepository,
            RoomMapper roomMapper
    ) {
        this.roomRepository = roomRepository;
        this.roomStatusRepository = roomStatusRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.roomMapper = roomMapper;
    }

    public Slice<RoomListItem> getRooms(Pageable pageable) {
        return roomRepository.findAllRooms(pageable);
    }

    public RoomListItem addRoom(RoomCreateRequest createRequest) {
        Room room = roomMapper.toEntity(createRequest);

        if (roomRepository.existsByName(room.getName()))
            throw new ResourceAlreadyExistsException("Room with name " + room.getName() + " already exists");

        RoomType type = roomTypeRepository.findById(createRequest.typeId())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid room type ID"));

        RoomStatus defaultStatus = roomStatusRepository.findByName("AVAILABLE")
                .orElseThrow(() -> new IllegalStateException("Default status not found"));

        room.setType(type);
        room.setStatus(defaultStatus);

        Room savedRoom = roomRepository.save(room);
        return roomMapper.toDto(savedRoom);
    }
}
