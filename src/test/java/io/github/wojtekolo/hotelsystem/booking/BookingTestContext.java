package io.github.wojtekolo.hotelsystem.booking;

import io.github.wojtekolo.hotelsystem.customer.Customer;
import io.github.wojtekolo.hotelsystem.employee.Employee;
import io.github.wojtekolo.hotelsystem.room.Room;

public record BookingTestContext(
        Customer customer,
        Employee employee,
        Room room
) {
}

