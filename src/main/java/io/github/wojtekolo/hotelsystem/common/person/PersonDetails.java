package io.github.wojtekolo.hotelsystem.common.person;

import java.time.LocalDate;

public record PersonDetails (
        Long id,
        String name,
        String surname,
        LocalDate birthDate
) {
}
