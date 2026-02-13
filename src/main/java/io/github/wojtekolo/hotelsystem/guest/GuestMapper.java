package io.github.wojtekolo.hotelsystem.guest;

import io.github.wojtekolo.hotelsystem.common.person.PersonMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = PersonMapper.class)
public interface GuestMapper {

    @Mapping(target = "id", ignore = true)
    Guest toEntity(GuestCreateRequest createRequest);

    GuestDetails toDetails(Guest guest);
}
