package io.github.wojtekolo.hotelsystem.person.api;

import java.time.LocalDate;

public record PersonDetails (
        Long id,
        String name,
        String surname,
        LocalDate birthDate
) {
}
