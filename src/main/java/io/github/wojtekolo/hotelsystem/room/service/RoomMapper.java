package io.github.wojtekolo.hotelsystem.room.service;

import io.github.wojtekolo.hotelsystem.room.api.RoomCreateRequest;
import io.github.wojtekolo.hotelsystem.room.api.RoomDetails;
import io.github.wojtekolo.hotelsystem.room.api.RoomListItem;
import io.github.wojtekolo.hotelsystem.room.api.RoomTypeDto;
import io.github.wojtekolo.hotelsystem.room.model.Room;
import io.github.wojtekolo.hotelsystem.room.model.RoomType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoomMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "lifecycleStatus", ignore = true)
    @Mapping(target = "operationalStatus", ignore = true)
    Room toEntity(RoomCreateRequest dto);

    @Mapping(source = "type.name", target = "type")
    @Mapping(source = "operationalStatus", target = "operationalStatus")
    @Mapping(source = "lifecycleStatus", target = "lifecycleStatus")
    @Mapping(source = "type.pricePerNight", target = "pricePerNight")
    @Mapping(source = "type.capacity", target = "capacity")
    RoomListItem toDto(Room room);

    RoomDetails toDetailsDto(Room room);

    RoomTypeDto toRoomTypeDto(RoomType roomType);
}
