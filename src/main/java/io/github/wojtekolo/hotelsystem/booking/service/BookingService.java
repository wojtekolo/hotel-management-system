package io.github.wojtekolo.hotelsystem.booking.service;

import io.github.wojtekolo.hotelsystem.booking.api.request.BookingCreateRequest;
import io.github.wojtekolo.hotelsystem.booking.api.request.BookingUpdateRequest;
import io.github.wojtekolo.hotelsystem.booking.api.response.BookingDetails;
import io.github.wojtekolo.hotelsystem.booking.exception.details.RoomStayViolationDetails;
import io.github.wojtekolo.hotelsystem.booking.persistence.BookingRepository;
import io.github.wojtekolo.hotelsystem.booking.model.entity.Booking;
import io.github.wojtekolo.hotelsystem.booking.service.loading.BookingResourceLoader;
import io.github.wojtekolo.hotelsystem.booking.service.loading.BookingResources;
import io.github.wojtekolo.hotelsystem.booking.service.processing.BookingStayProcessor;
import io.github.wojtekolo.hotelsystem.booking.service.validation.BookingValidationResult;
import io.github.wojtekolo.hotelsystem.booking.service.validation.BookingValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

 import java.util.*;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingMapper bookingMapper;
    private final BookingRepository bookingRepository;
    private final BookingValidator bookingValidator;
    private final BookingStayProcessor bookingStayProcessor;
    private final BookingResourceLoader bookingResourceLoader;

    @Transactional
    public BookingDetails addBooking(BookingCreateRequest request) {
        BookingResources resources = bookingResourceLoader.loadForCreate(request);
        Booking booking = Booking.createDefault(resources.customer(), resources.employee());

        List<RoomStayViolationDetails> updateErrors = bookingStayProcessor.createBooking(
                booking, request.stays(), resources.employee(), resources.roomLoad().rooms());

        BookingValidationResult validationResult = bookingValidator.validateBooking(booking);

        if (validationResult.hasErrors() || !updateErrors.isEmpty() || !resources.integrityErrors().isEmpty())
            throw validationResult.toException("Error creating booking", updateErrors, resources.integrityErrors());

        booking = bookingRepository.save(booking);

        return bookingMapper.toBookingDetails(booking);
    }

    @Transactional
    public BookingDetails updateBooking(Long bookingId, BookingUpdateRequest request) {
        BookingResources resources = bookingResourceLoader.loadForUpdate(bookingId, request);

        List<RoomStayViolationDetails> updateErrors = bookingStayProcessor.updateBooking(
                resources.booking(), request.stays(), resources.employee(), resources.roomLoad().rooms());

        BookingValidationResult validationResult = bookingValidator.validateBooking(resources.booking());

        if (validationResult.hasErrors() || !updateErrors.isEmpty() || !resources.integrityErrors().isEmpty())
            throw validationResult.toException("Error updating booking", updateErrors, resources.integrityErrors());

        return bookingMapper.toBookingDetails(resources.booking());
    }

    @Transactional
    public BookingDetails getBooking(Long bookingId) {
        return bookingMapper.toBookingDetails(bookingResourceLoader.loadBooking(bookingId));
    }
}
