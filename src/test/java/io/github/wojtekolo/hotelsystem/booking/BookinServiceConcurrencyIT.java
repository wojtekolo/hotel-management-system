package io.github.wojtekolo.hotelsystem.booking;

import io.github.wojtekolo.hotelsystem.booking.api.BookingCreateRequest;
import io.github.wojtekolo.hotelsystem.booking.api.BookingUpdateRequest;
import io.github.wojtekolo.hotelsystem.booking.exception.BookingValidationException;
import io.github.wojtekolo.hotelsystem.booking.persistence.BookingRepository;
import io.github.wojtekolo.hotelsystem.booking.persistence.RoomStayRepository;
import io.github.wojtekolo.hotelsystem.booking.service.BookingService;
import io.github.wojtekolo.hotelsystem.common.TestDataFactory;
import io.github.wojtekolo.hotelsystem.customer.model.Customer;
import io.github.wojtekolo.hotelsystem.employee.model.Employee;
import io.github.wojtekolo.hotelsystem.room.model.Room;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class BookinServiceConcurrencyIT {
    @Autowired
    BookingService bookingService;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private RoomStayRepository roomStayRepository;
    @Autowired
    TestDataFactory data;

    @Autowired
    EntityManager entityManager;

    @AfterEach
    void cleanUp() {
        roomStayRepository.deleteAll();
        bookingRepository.deleteAll();
    }

    @Test
    void should_save_only_one_booking_when_trying_concurrent_colliding_create_requests() throws InterruptedException {
//        given
        int requestCount = 10;
        Room room = data.prepareRoom();
        Customer customer = data.prepareCustomer();
        Employee employee = data.prepareEmployee();

        List<BookingCreateRequest> requests = data.prepareCollidingCreateRequests(requestCount, room, customer, employee);


        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(requestCount);

        List<BookingValidationException> exceptions = Collections.synchronizedList(new ArrayList<>());

        entityManager.clear();

//        when
        try (ExecutorService executor = Executors.newFixedThreadPool(requestCount)) {
            for (BookingCreateRequest request : requests) {
                executor.execute(() -> {
                    try {
                        startLatch.await();
                        bookingService.addBooking(request);
                    } catch (BookingValidationException e) {
                        exceptions.add(e);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        finishLatch.countDown();
                    }
                });
            }
            startLatch.countDown();

            if (!finishLatch.await(10, TimeUnit.SECONDS)) {
                throw new AssertionError("Time limit exceeded");
            }
        }

        // then
        assertThat(exceptions).hasSize(requestCount - 1);
        assertThat(bookingRepository.findAll()).hasSize(1);
        assertThat(roomStayRepository.findAll()).hasSize(1);
    }

    @Test
    void should_update_only_one_booking_when_concurrent_colliding_update_requests() throws InterruptedException {
//        given
        int requestCount = 10;
        Customer customer = data.prepareCustomer();
        Employee employee = data.prepareEmployee();
        Room targetRoom = data.prepareRoom();

        List<BookingUpdateRequest> requests = data.prepareCollidingUpdateRequests(requestCount, targetRoom, customer, employee);


        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(requestCount);

        List<BookingValidationException> exceptions = Collections.synchronizedList(new ArrayList<>());

        entityManager.clear();

//        when
        try (ExecutorService executor = Executors.newFixedThreadPool(requestCount)) {
            for (BookingUpdateRequest request : requests) {
                executor.execute(() -> {
                    try {
                        startLatch.await();
                        bookingService.updateBooking(request);
                    } catch (BookingValidationException e) {
                        exceptions.add(e);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        finishLatch.countDown();
                    }
                });
            }
            startLatch.countDown();

            if (!finishLatch.await(10, TimeUnit.SECONDS)) {
                throw new AssertionError("Time limit exceeded");
            }
        }

        // then
        assertThat(exceptions).hasSize(requestCount - 1);
        assertThat(roomStayRepository.countByRoomId(targetRoom.getId())).isEqualTo(1);
    }
}
