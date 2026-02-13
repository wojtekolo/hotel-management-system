package io.github.wojtekolo.hotelsystem.common.person;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DocumentRequest(
        @NotNull
        DocumentType type,
        @NotBlank
        String number
) {
}
