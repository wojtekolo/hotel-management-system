package io.github.wojtekolo.hotelsystem.room.service;

import io.github.wojtekolo.hotelsystem.booking.service.loading.RoomLoadResult;
import io.github.wojtekolo.hotelsystem.common.exceptions.ResourceAlreadyExistsException;
import io.github.wojtekolo.hotelsystem.common.exceptions.ResourceNotFoundException;
import io.github.wojtekolo.hotelsystem.room.persistence.RoomRepository;
import io.github.wojtekolo.hotelsystem.room.persistence.RoomTypeRepository;
import io.github.wojtekolo.hotelsystem.room.api.RoomCreateRequest;
import io.github.wojtekolo.hotelsystem.room.api.RoomDetails;
import io.github.wojtekolo.hotelsystem.room.api.RoomListItem;
import io.github.wojtekolo.hotelsystem.room.model.LifecycleStatus;
import io.github.wojtekolo.hotelsystem.room.model.OperationalStatus;
import io.github.wojtekolo.hotelsystem.room.model.Room;
import io.github.wojtekolo.hotelsystem.room.model.RoomType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    public RoomLoadResult findAndLockRooms(Set<Long> ids){
        List<Long> sortedIds = ids.stream().sorted().toList();
        List<Room> rooms = roomRepository.findAllByIdWithLock(sortedIds);

        Map<Long, Room> loadedRooms = rooms.stream().collect(Collectors.toMap(Room::getId, room -> room));

        Set<Long> missingIds = sortedIds.stream()
                .filter(id -> !loadedRooms.containsKey(id)).collect(Collectors.toSet());

        return new RoomLoadResult(loadedRooms, missingIds);
    }
}
