package io.github.wojtekolo.hotelsystem.booking.persistence;

import io.github.wojtekolo.hotelsystem.booking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
}
