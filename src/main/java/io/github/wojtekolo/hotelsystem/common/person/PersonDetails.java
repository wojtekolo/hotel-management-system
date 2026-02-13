package io.github.wojtekolo.hotelsystem.common.person;

import java.util.List;

public record PersonDetails (
        Long id,
        String name,
        String surname,
        String email,
        List<PhoneNumberDetails> phoneNumbers,
        List<PersonalDocumentDetails> personalDocuments
) {
}
