package io.github.wojtekolo.hotelsystem.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingDetails> addBooking(@Valid @RequestBody BookingCreateRequest createRequest){
        BookingDetails details = bookingService.addBooking(createRequest);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(details.id())
                .toUri();

        return ResponseEntity.created(location).body(details);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookingDetails> updateBooking(@Valid @RequestBody BookingUpdateRequest updateRequest){
        BookingDetails details = bookingService.updateBooking(updateRequest);

        return ResponseEntity.ok(details);
    }
}
