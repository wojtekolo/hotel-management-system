package io.github.wojtekolo.hotelsystem.customer;

import io.github.wojtekolo.hotelsystem.common.person.PersonDetails;

public record CustomerDetails(
        Long id,
        PersonDetails person,
        String description,
        LoyaltyStatus loyaltyStatus,
        String privateEmail,
        String privatePhone
        ) {
}
