package io.github.wojtekolo.hotelsystem.booking;

import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class BookingService {
    public BookingDetails addBooking(BookingCreateRequest request){
        return new BookingDetails(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                new ArrayList<>()
        );
    }
}
