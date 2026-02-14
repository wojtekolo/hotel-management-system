package io.github.wojtekolo.hotelsystem.common.person;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PersonMapper {

    @Mapping(target = "id", ignore = true)
    Person toEntity(PersonCreateRequest personCreateRequest);

    PersonDetails toPersonDetails(Person person);
}
