package io.github.wojtekolo.hotelsystem.booking.service;

import io.github.wojtekolo.hotelsystem.AbstractIntegrationTest;
import io.github.wojtekolo.hotelsystem.booking.api.request.BookingCreateRequest;
import io.github.wojtekolo.hotelsystem.booking.api.request.BookingUpdateRequest;
import io.github.wojtekolo.hotelsystem.booking.model.entity.Booking;
import io.github.wojtekolo.hotelsystem.booking.persistence.RoomStayRepository;
import io.github.wojtekolo.hotelsystem.booking.service.occupancy.RoomOccupancyCacheService;
import io.github.wojtekolo.hotelsystem.booking.service.occupancy.RoomOccupancyService;
import io.github.wojtekolo.hotelsystem.common.DataBaseCleaner;
import io.github.wojtekolo.hotelsystem.common.TestDataFactory;
import io.github.wojtekolo.hotelsystem.customer.model.Customer;
import io.github.wojtekolo.hotelsystem.employee.model.Employee;
import io.github.wojtekolo.hotelsystem.room.api.OccupancyQuery;
import io.github.wojtekolo.hotelsystem.room.model.Room;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static io.github.wojtekolo.hotelsystem.booking.BookingTestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;

public class BookingServiceCacheIntegrationTest  extends AbstractIntegrationTest {
    private final LocalDate today = LocalDate.now();
    @Autowired
    TestDataFactory data;

    @Autowired
    BookingService bookingService;

    @Autowired
    private CacheManager cacheManager;

    @MockitoSpyBean
    RoomStayRepository roomStayRepository;

    @Autowired
    private DataBaseCleaner dataBaseCleaner;

    @Autowired
    private RoomOccupancyService occupancyService;

    @AfterEach
    void cleanUp() {
        dataBaseCleaner.cleanUp();
    }

    @BeforeEach
    void setup() {
        Objects.requireNonNull(cacheManager.getCache(RoomOccupancyCacheService.CACHE_NAME)).clear();
    }

    @Test
    public void should_evict_cache_when_creating_booking(){
//        given
        Room room = data.prepareRoom();
        Employee employee = data.prepareEmployee();
        Customer customer = data.prepareCustomer();
        data.prepareBooking(room, customer, employee, today.plusDays(10), today.plusDays(15));

//        load cache
        loadCache(room.getId());

        var bookingCreateRequest = new BookingCreateRequest(customer.getId(), employee.getId(), List.of(
                createRoomStayCreateRequest(room.getId(), today.plusDays(5), today.plusDays(10))
        ));

//        when
        bookingService.addBooking(bookingCreateRequest);

//        then
        assertThat(cacheManager.getCache(RoomOccupancyCacheService.CACHE_NAME).get(room.getId())).isNull();
    }

    @Test
    public void should_evict_cache_for_old_and_new_room_when_deleting_and_adding_room_stay_to_existing_booking(){
//        given
        Room room1 = data.prepareRoom();
        Room room2 = data.prepareRoom();
        Employee employee = data.prepareEmployee();
        Customer customer = data.prepareCustomer();
        data.prepareBooking(room1, customer, employee, today.plusDays(10), today.plusDays(15));
        Booking bookingToUpdate = data.prepareBooking(room2, customer, employee, today.plusDays(10), today.plusDays(15));

//        load cache
        assertThat(cacheManager.getCache(RoomOccupancyCacheService.CACHE_NAME)).isNotNull();

        loadCache(room1.getId());
        loadCache(room2.getId());

        BookingUpdateRequest updateRequest = createBookingUpdateRequest(
                employee.getId(),
                List.of(
                        createRoomStayUpdateRequest(null,
                                room1.getId(), today.plusDays(20), today.plusDays(25))
                )
        );

//        when
        bookingService.updateBooking(bookingToUpdate.getId(), updateRequest);

//        then
        assertThat(cacheManager.getCache(RoomOccupancyCacheService.CACHE_NAME).get(room1.getId())).isNull();
        assertThat(cacheManager.getCache(RoomOccupancyCacheService.CACHE_NAME).get(room2.getId())).isNull();
    }

    @Test
    public void should_evict_cache_when_updating_existing_room_stay(){
//        given
        Room room = data.prepareRoom();
        Employee employee = data.prepareEmployee();
        Customer customer = data.prepareCustomer();
        Booking bookingToUpdate = data.prepareBooking(room, customer, employee, today.plusDays(10), today.plusDays(15));

//        load cache
        assertThat(cacheManager.getCache(RoomOccupancyCacheService.CACHE_NAME)).isNotNull();

        loadCache(room.getId());

        BookingUpdateRequest updateRequest = createBookingUpdateRequest(
                employee.getId(),
                List.of(
                        createRoomStayUpdateRequest(bookingToUpdate.getStays().getFirst().getId(),
                                room.getId(), today.plusDays(20), today.plusDays(25))
                )
        );

//        when
        bookingService.updateBooking(bookingToUpdate.getId(), updateRequest);

//        then
        assertThat(cacheManager.getCache(RoomOccupancyCacheService.CACHE_NAME).get(room.getId())).isNull();
    }

    private void loadCache(Long roomId) {
        assertThat(cacheManager.getCache(RoomOccupancyCacheService.CACHE_NAME)).isNotNull();
        assertThat(cacheManager.getCache(RoomOccupancyCacheService.CACHE_NAME).get(roomId)).isNull();

        occupancyService.getRoomOccupancy(roomId, new OccupancyQuery(today, today.plusDays(30), null));

        org.awaitility.Awaitility.await()
                .atMost(java.time.Duration.ofSeconds(2))
                .until(() -> cacheManager.getCache(RoomOccupancyCacheService.CACHE_NAME).get(roomId) != null);

        assertThat(cacheManager.getCache(RoomOccupancyCacheService.CACHE_NAME).get(roomId)).isNotNull();
    }

}
