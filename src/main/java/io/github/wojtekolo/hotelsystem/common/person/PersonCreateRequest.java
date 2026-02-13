package io.github.wojtekolo.hotelsystem.common.person;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record PersonCreateRequest (

        @NotBlank
        String name,

        @NotBlank
        String surname,

        @NotBlank
        @Email
        String email,

        @NotEmpty
        @Valid
        List<PhoneRequest> phoneNumbers,

        @NotEmpty
        @Valid
        List<DocumentRequest> personalDocuments
) {
}
