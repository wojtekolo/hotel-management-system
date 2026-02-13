package io.github.wojtekolo.hotelsystem.common.person;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PersonMapper {

    @Mapping(target = "id", ignore = true)
    Person toEntity(PersonCreateRequest personCreateRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "person", ignore = true)
    PhoneNumber toPhoneEntity(PhoneRequest phoneRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "person", ignore = true)
    PersonalDocument toDocumentEntity(DocumentRequest documentRequest);

    @AfterMapping
    default void mapPhonesAndDocuments(@MappingTarget Person person){
        if (person.getPhoneNumbers() != null){
            person.getPhoneNumbers().forEach(phoneNumber -> phoneNumber.setPerson(person));
        }
        if (person.getPersonalDocuments() != null){
            person.getPersonalDocuments().forEach(personalDocument -> personalDocument.setPerson(person));
        }
    }

    PhoneNumberDetails toPhoneDetails(PhoneNumber phoneNumber);

    PersonalDocumentDetails toDocumentDetails(PersonalDocument personalDocument);

    PersonDetails toPersonDetails(Person person);
}
