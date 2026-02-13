package io.github.wojtekolo.hotelsystem.guest;

import io.github.wojtekolo.hotelsystem.common.person.PersonCreateRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record GuestCreateRequest(
        @Valid
        @NotNull
        PersonCreateRequest person,
        String description
) {
}
