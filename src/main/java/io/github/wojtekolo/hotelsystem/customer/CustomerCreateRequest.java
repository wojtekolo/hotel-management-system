package io.github.wojtekolo.hotelsystem.customer;

import io.github.wojtekolo.hotelsystem.common.person.PersonCreateRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CustomerCreateRequest(
        @Valid
        @NotNull(message = "Person data is required")
        PersonCreateRequest person,

        String description,

        @Email(message = "Email must be correctly formatted")
        @NotBlank(message = "Email is required")
        String privateEmail,

        @NotBlank(message = "Phone number is required")
        String privatePhone
) {
}
