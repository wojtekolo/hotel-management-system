package io.github.wojtekolo.hotelsystem.booking;

import io.github.wojtekolo.hotelsystem.customer.model.Customer;
import io.github.wojtekolo.hotelsystem.employee.model.Employee;
import io.github.wojtekolo.hotelsystem.room.model.Room;

public record BookingTestContext(
        Customer customer,
        Employee employee,
        Room room
) {
}

