package io.github.wojtekolo.hotelsystem.guest;

import io.github.wojtekolo.hotelsystem.common.person.PersonDetails;

public record GuestDetails(
        Long id,
        String description,
        LoyaltyStatus loyaltyStatus,
        PersonDetails person
        ) {
}
