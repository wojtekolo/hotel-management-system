package io.github.wojtekolo.hotelsystem.booking.service.occupancy;

import io.github.wojtekolo.hotelsystem.AbstractIntegrationTest;
import io.github.wojtekolo.hotelsystem.booking.api.response.OccupiedRange;
import io.github.wojtekolo.hotelsystem.booking.api.response.RoomOccupancyResponse;
import io.github.wojtekolo.hotelsystem.booking.model.entity.RoomStayStatus;
import io.github.wojtekolo.hotelsystem.booking.persistence.RoomStayRepository;
import io.github.wojtekolo.hotelsystem.common.DataBaseCleaner;
import io.github.wojtekolo.hotelsystem.common.TestDataFactory;
import io.github.wojtekolo.hotelsystem.room.model.Room;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.*;


class RoomOccupancyCacheServiceIntegrationTest extends AbstractIntegrationTest {
    private final LocalDate today = LocalDate.now();
    @Autowired
    TestDataFactory data;

    @Autowired
    RoomOccupancyCacheService cacheService;

    @Autowired
    private CacheManager cacheManager;

    @MockitoSpyBean
    RoomStayRepository roomStayRepository;

    @Autowired
    private DataBaseCleaner dataBaseCleaner;

    @AfterEach
    void cleanUp() {
        dataBaseCleaner.cleanUp();
    }

    @Test
    public void should_use_cached_data_when_not_possible() {
//        given
        Room room = data.prepareRoom();
        var employee = data.prepareEmployee();
        var customer = data.prepareCustomer();
        var booking1 = data.prepareBooking(room, customer, employee,
                today.plusDays(10), today.plusDays(20));
        var booking2 = data.prepareBooking(room, customer, employee,
                today.plusDays(30), today.plusDays(40));

        LocalDate maxDate = today.plusDays(100);

//        when
        RoomOccupancyResponse result = cacheService.getAllRoomOccupancy(room.getId(), today, maxDate);

//        then
        verify(roomStayRepository, times(1))
                .getOccupiedRangesForRoom(room.getId(), RoomStayStatus.COLLIDING_STATUSES, today, maxDate);
        assertThat(result.occupiedRanges()).extracting(OccupiedRange::from, OccupiedRange::to, OccupiedRange::bookingId)
                .containsExactlyInAnyOrder(
                        tuple(today.plusDays(10), today.plusDays(20), booking1.getId()),
                        tuple(today.plusDays(30), today.plusDays(40), booking2.getId())
                );
    }

    @Test
    public void should_use_cached_data_when_possible() {
//        given
        Room room = data.prepareRoom();
        var employee = data.prepareEmployee();
        var customer = data.prepareCustomer();
        var booking1 = data.prepareBooking(room, customer, employee,
                today.plusDays(10), today.plusDays(20));
        var booking2 = data.prepareBooking(room, customer, employee,
                today.plusDays(30), today.plusDays(40));

        LocalDate maxDate = today.plusDays(100);

//        load cache
        cacheService.getAllRoomOccupancy(room.getId(), today, maxDate);

        Awaitility.await().atMost(java.time.Duration.ofSeconds(2))
                .until(() -> cacheManager.getCache(RoomOccupancyCacheService.CACHE_NAME).get(room.getId()) != null);

//          when
        RoomOccupancyResponse result = cacheService.getAllRoomOccupancy(room.getId(), today, maxDate);

//        then
        verify(roomStayRepository, times(1))
                .getOccupiedRangesForRoom(room.getId(), RoomStayStatus.COLLIDING_STATUSES, today, maxDate);

        assertThat(result.occupiedRanges()).extracting(OccupiedRange::from, OccupiedRange::to, OccupiedRange::bookingId)
                .containsExactlyInAnyOrder(
                        tuple(today.plusDays(10), today.plusDays(20), booking1.getId()),
                        tuple(today.plusDays(30), today.plusDays(40), booking2.getId())
                );
    }
}