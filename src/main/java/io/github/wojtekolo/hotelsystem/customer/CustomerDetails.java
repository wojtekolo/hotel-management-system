package io.github.wojtekolo.hotelsystem.customer;

import io.github.wojtekolo.hotelsystem.common.person.PersonDetails;

public record CustomerDetails(
        Long id,
        String description,
        LoyaltyStatus loyaltyStatus,
        PersonDetails person
        ) {
}
