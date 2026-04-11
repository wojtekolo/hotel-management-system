package io.github.wojtekolo.hotelsystem.booking.service.loading;

import io.github.wojtekolo.hotelsystem.booking.exception.details.IntegrityViolationDetails;
import io.github.wojtekolo.hotelsystem.booking.model.entity.Booking;
import io.github.wojtekolo.hotelsystem.customer.model.Customer;
import io.github.wojtekolo.hotelsystem.employee.model.Employee;

import java.util.List;

public record BookingResources(
        Booking booking,
        RoomLoadResult roomLoad,
        Employee employee,
        Customer customer,
        List<IntegrityViolationDetails> integrityErrors
) {}
