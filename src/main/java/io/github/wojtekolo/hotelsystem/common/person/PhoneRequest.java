package io.github.wojtekolo.hotelsystem.common.person;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PhoneRequest(
        @NotNull
        PhoneType type,
        @NotBlank
        String number
) {
}
