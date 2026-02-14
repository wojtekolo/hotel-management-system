package io.github.wojtekolo.hotelsystem.common.person;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record PersonCreateRequest (

        @NotBlank(message = "Person name is required")
        String name,

        @NotBlank(message = "Person surname is required")
        String surname,

        @NotNull(message = "Person birth date is required")
        @Past(message = "Birth date must be in the past")
        LocalDate birthDate
) {
}
