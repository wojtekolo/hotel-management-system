package io.github.wojtekolo.hotelsystem.booking.exception.details;

import java.util.Map;

public record IntegrityViolationDetails(
        IntegrityErrorCode code,
        Map<String, Object> context
) {
}
