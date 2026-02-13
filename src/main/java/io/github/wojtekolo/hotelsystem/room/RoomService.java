package io.github.wojtekolo.hotelsystem.room;

import io.github.wojtekolo.hotelsystem.common.exceptions.ResourceAlreadyExistsException;
import io.github.wojtekolo.hotelsystem.common.exceptions.ResourceNotFoundException;
import io.github.wojtekolo.hotelsystem.room.dtos.RoomCreateRequest;
import io.github.wojtekolo.hotelsystem.room.dtos.RoomDetails;
import io.github.wojtekolo.hotelsystem.room.dtos.RoomListItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
public class RoomService {
    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final RoomMapper roomMapper;

    public RoomService(
            RoomRepository roomRepository,
            RoomTypeRepository roomTypeRepository,
            RoomMapper roomMapper
    ) {
        this.roomRepository = roomRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.roomMapper = roomMapper;
    }

    public Slice<RoomListItem> getRooms(Pageable pageable) {
        return roomRepository.findAllRooms(pageable);
    }

    public RoomDetails addRoom(RoomCreateRequest createRequest) {
        Room room = roomMapper.toEntity(createRequest);

        if (roomRepository.existsByName(room.getName()))
            throw new ResourceAlreadyExistsException("Room with name " + room.getName() + " already exists");

        RoomType type = roomTypeRepository.findById(createRequest.typeId())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid room type ID"));

        room.setType(type);
        room.setLifecycleStatus(LifecycleStatus.ACTIVE);
        room.setOperationalStatus(OperationalStatus.CLEAN);

        Room savedRoom = roomRepository.save(room);
        return roomMapper.toDetailsDto(savedRoom);
    }
}
