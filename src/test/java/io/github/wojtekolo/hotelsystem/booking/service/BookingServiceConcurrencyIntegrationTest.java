package io.github.wojtekolo.hotelsystem.booking.service;

import io.github.wojtekolo.hotelsystem.booking.api.request.BookingCreateRequest;
import io.github.wojtekolo.hotelsystem.booking.api.request.BookingUpdateRequest;
import io.github.wojtekolo.hotelsystem.booking.exception.BookingValidationException;
import io.github.wojtekolo.hotelsystem.booking.persistence.BookingRepository;
import io.github.wojtekolo.hotelsystem.booking.persistence.RoomStayRepository;
import io.github.wojtekolo.hotelsystem.common.TestDataFactory;
import io.github.wojtekolo.hotelsystem.customer.model.Customer;
import io.github.wojtekolo.hotelsystem.customer.persistence.CustomerRepository;
import io.github.wojtekolo.hotelsystem.customer.persistence.LoyaltyStatusRepository;
import io.github.wojtekolo.hotelsystem.employee.model.Employee;
import io.github.wojtekolo.hotelsystem.employee.persistence.EmployeeRepository;
import io.github.wojtekolo.hotelsystem.person.persistence.PersonRepository;
import io.github.wojtekolo.hotelsystem.room.model.Room;
import io.github.wojtekolo.hotelsystem.room.persistence.RoomRepository;
import io.github.wojtekolo.hotelsystem.room.persistence.RoomTypeRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
public class BookingServiceConcurrencyIntegrationTest {
    @Autowired
    BookingService bookingService;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private RoomStayRepository roomStayRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private LoyaltyStatusRepository loyaltyStatusRepository;
    @Autowired
    private RoomTypeRepository roomTypeRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    TestDataFactory data;

    @Autowired
    EntityManager entityManager;

    @AfterEach
    void cleanUp() {
        roomStayRepository.deleteAll();
        bookingRepository.deleteAll();
        employeeRepository.deleteAll();
        customerRepository.deleteAll();
        personRepository.deleteAll();
        loyaltyStatusRepository.deleteAll();
        roomRepository.deleteAll();
        roomTypeRepository.deleteAll();
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

        Map<Long, BookingUpdateRequest> requests = data.prepareCollidingUpdateRequests(requestCount, targetRoom, customer, employee);


        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(requestCount);

        List<BookingValidationException> exceptions = Collections.synchronizedList(new ArrayList<>());

        entityManager.clear();

//        when
        try (ExecutorService executor = Executors.newFixedThreadPool(requestCount)) {
            for (Map.Entry<Long, BookingUpdateRequest> entry : requests.entrySet()) {
                executor.execute(() -> {
                    try {
                        startLatch.await();
                        bookingService.updateBooking(entry.getKey(), entry.getValue());
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
