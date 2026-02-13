package io.github.wojtekolo.hotelsystem.room;

import io.github.wojtekolo.hotelsystem.room.dtos.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoomMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "status", ignore = true)
    Room toEntity(RoomCreateRequest dto);

    @Mapping(source = "type.name", target = "type")
    @Mapping(source = "status.name", target = "status")
    @Mapping(source = "type.pricePerNight", target = "pricePerNight")
    @Mapping(source = "type.capacity", target = "capacity")
    RoomListItem toDto(Room room);

    RoomDetails toDetailsDto(Room room);

    RoomTypeDto toRoomTypeDto(RoomType roomType);

    RoomStatusDto toRoomStatusDto(RoomStatus roomStatus);
}
