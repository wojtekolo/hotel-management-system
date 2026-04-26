package io.github.wojtekolo.hotelsystem.booking.dev;

import io.github.wojtekolo.hotelsystem.booking.model.entity.Booking;
import io.github.wojtekolo.hotelsystem.booking.model.entity.RoomStay;
import io.github.wojtekolo.hotelsystem.booking.model.entity.RoomStayStatus;
import io.github.wojtekolo.hotelsystem.customer.model.Customer;
import io.github.wojtekolo.hotelsystem.employee.model.Employee;
import io.github.wojtekolo.hotelsystem.room.model.Room;
import io.github.wojtekolo.hotelsystem.room.model.RoomType;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
@Profile("seed")
@RequiredArgsConstructor
public class PerformanceDataSeeder implements CommandLineRunner {

    private final EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) {
        ensureRoomsExist(1000);
        Long bookingCount = entityManager.createQuery("SELECT COUNT(b) FROM Booking b", Long.class).getSingleResult();

        if (bookingCount >= 200000) {
            seedCancelledBookings();
        } else {
            seedCancelledBookings();
            seedPlannedBookings();
        }
    }

    @Transactional
    public void seedCancelledBookings() {
        List<Room> rooms = entityManager.createQuery("SELECT r FROM Room r", Room.class).getResultList();

        Customer customer = getCustomer();
        Employee employee = getEmployee();

        int batchSize = 1000;
        int total = 0;

        for (Room room : rooms) {

            for (int i = 0; i < 100; i++) {
                LocalDate start = LocalDate.now().minusDays(ThreadLocalRandom.current().nextInt(1, 365));
                LocalDate end = start.plusDays(2);

                Booking booking = Booking.createDefault(customer, employee);

                RoomStay stay = RoomStay.builder()
                        .booking(booking)
                        .room(room)
                        .createBy(employee)
                        .activeFrom(start)
                        .activeTo(end)
                        .status(RoomStayStatus.CANCELLED)
                        .pricePerNight(BigDecimal.valueOf(250.00))
                        .build();

                booking.addStay(stay);
                entityManager.persist(booking);
                total++;
                if (total % 10000 == 0) {
                    System.out.println("Saved: " + total + " / 100 000");
                }

                if (total % batchSize == 0) {
                    entityManager.flush();
                    entityManager.clear();
                    customer = entityManager.merge(customer);
                    employee = entityManager.merge(employee);
                }
            }
        }
    }

    @Transactional
    public void seedPlannedBookings(){
        List<Room> rooms = entityManager.createQuery("SELECT r FROM Room r", Room.class).getResultList();

        Customer customer = getCustomer();
        Employee employee = getEmployee();

        int batchSize = 1000;
        int totalSaved = 0;

        for (Room room : rooms) {
            LocalDate currentPointer = LocalDate.now();

            for (int j = 0; j < 200; j++) {
                LocalDate start = currentPointer;
                LocalDate end = start.plusDays(2);

                currentPointer = end;

                Booking booking = Booking.createDefault(customer, employee);
                RoomStay stay = RoomStay.createPlanned(
                        booking,
                        room,
                        BigDecimal.ZERO,
                        employee,
                        start,
                        end,
                        BigDecimal.valueOf(250.00)
                );

                booking.addStay(stay);
                entityManager.persist(booking);

                totalSaved++;

                if (totalSaved % batchSize == 0) {
                    entityManager.flush();
                    entityManager.clear();
                    customer = entityManager.merge(customer);
                    employee = entityManager.merge(employee);
                }
            }

            if (totalSaved % 10000 == 0) {
                System.out.println("Saved: " + totalSaved + " / 200 000");
            }
        }
    }

    private void ensureRoomsExist(int required) {
        Long count = entityManager.createQuery("SELECT COUNT(r) FROM Room r", Long.class).getSingleResult();
        RoomType roomType = entityManager.createQuery("SELECT rt FROM RoomType rt", RoomType.class).getSingleResult();
        if (count < required) {
            for (int i = 0; i < (required - count); i++) {
                Room room = Room.builder().name("R-" + (count + i)).floor(1).type(roomType).build();
                entityManager.persist(room);
            }
            entityManager.flush();
        }
    }

    private Customer getCustomer() {
        return entityManager.createQuery("SELECT c FROM Customer c", Customer.class)
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Customers don't exist"));
    }

    private Employee getEmployee() {
        return entityManager.createQuery("SELECT e FROM Employee e", Employee.class)
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Employees don't exist"));
    }
}