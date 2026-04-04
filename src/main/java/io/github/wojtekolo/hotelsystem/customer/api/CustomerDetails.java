package io.github.wojtekolo.hotelsystem.customer.api;

import io.github.wojtekolo.hotelsystem.common.person.PersonDetails;
import io.github.wojtekolo.hotelsystem.customer.model.LoyaltyStatus;

public record CustomerDetails(
        Long id,
        PersonDetails person,
        String description,
        LoyaltyStatus loyaltyStatus,
        String privateEmail,
        String privatePhone
        ) {
}
