package io.github.wojtekolo.hotelsystem.booking.service.validation;
import io.github.wojtekolo.hotelsystem.booking.model.entity.Booking;
import io.github.wojtekolo.hotelsystem.booking.model.entity.RoomStay;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BookingValidator {

    private final BookingInternalValidator internalValidator;
    private final BookingExternalValidator externalValidator;

    public BookingValidationResult validateBooking(Booking booking) {
        List<RoomStay> staysToValidate = booking.getStays().stream()
                .filter(stay -> stay.getActiveTo().isAfter(stay.getActiveFrom())).toList();
        return new BookingValidationResult(
                externalValidator.validate(staysToValidate, booking.getId()),
                internalValidator.validate(staysToValidate)
        );
    }
}
