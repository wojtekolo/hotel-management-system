package io.github.wojtekolo.hotelsystem.booking.api;

import io.github.wojtekolo.hotelsystem.booking.BookingTestUtils;
import io.github.wojtekolo.hotelsystem.booking.api.response.BookingDetails;
import io.github.wojtekolo.hotelsystem.booking.service.BookingService;
import io.github.wojtekolo.hotelsystem.common.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {
    @MockitoBean
    private BookingService bookingService;

    @MockitoBean
    private CacheManager cacheManager;

    @Autowired
    MockMvc mockMvc;

    @Test
    void should_return_details() throws Exception {
        BookingDetails details = BookingTestUtils.aValidBookingDetails();
        given(bookingService.getBooking(1L)).willReturn(details);

        mockMvc.perform(get("/api/v1/bookings/"+1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(details.id()))
                .andExpect(jsonPath("$.createBy").value(details.createBy()))
                .andExpect(jsonPath("$.stays").isArray())
                .andExpect(jsonPath("$.stays", hasSize(details.stays().size())))
                .andExpect(jsonPath("$.stays[0].id").value(details.stays().get(0).id()))
                .andExpect(jsonPath("$.stays[0].roomId").value(details.stays().get(0).roomId()))
                .andExpect(jsonPath("$.stays[0].pricePerNight").value(details.stays().get(0).pricePerNight()));
    }

    @Test
    void should_return_not_found_when_booking_does_not_exist() throws Exception {
        given(bookingService.getBooking(1L)).willThrow(new ResourceNotFoundException("Booking with ID 1 not found"));

        mockMvc.perform(get("/api/v1/bookings/"+1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}