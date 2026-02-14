package io.github.wojtekolo.hotelsystem.customer;

import io.github.wojtekolo.hotelsystem.common.person.PersonCreateRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record CustomerCreateRequest(
        @Valid
        @NotNull
        PersonCreateRequest person,
        String description
) {
}
