package io.github.wojtekolo.hotelsystem.booking.service.loading;

import io.github.wojtekolo.hotelsystem.booking.exception.details.IntegrityViolationDetails;
import io.github.wojtekolo.hotelsystem.booking.model.entity.Booking;
import io.github.wojtekolo.hotelsystem.customer.model.Customer;

import java.util.List;

public record BookingResources(
        Booking booking,
        RoomLoadResult roomLoad,
        Customer customer,
        List<IntegrityViolationDetails> integrityErrors
) {}
