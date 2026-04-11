package io.github.wojtekolo.hotelsystem.booking.service.validation;
import io.github.wojtekolo.hotelsystem.booking.model.entity.Booking;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingValidator {

    private final BookingInternalValidator internalValidator;
    private final BookingExternalValidator externalValidator;

    public BookingValidationResult validateBooking(Booking booking) {
        return new BookingValidationResult(
                externalValidator.validate(booking.getStays(), booking.getId()),
                internalValidator.validate(booking.getStays())
        );
    }
}
