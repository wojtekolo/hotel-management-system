package io.github.wojtekolo.hotelsystem.person.service;

import io.github.wojtekolo.hotelsystem.person.api.PersonCreateRequest;
import io.github.wojtekolo.hotelsystem.person.api.PersonDetails;
import io.github.wojtekolo.hotelsystem.person.model.Person;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PersonMapper {

    @Mapping(target = "id", ignore = true)
    Person toEntity(PersonCreateRequest personCreateRequest);

    PersonDetails toPersonDetails(Person person);
}
