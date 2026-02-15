package io.github.wojtekolo.hotelsystem.booking;

import io.github.wojtekolo.hotelsystem.customer.Customer;
import io.github.wojtekolo.hotelsystem.employee.Employee;
import io.github.wojtekolo.hotelsystem.room.Room;
import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @FutureOrPresent
    @Column(nullable = false)
    private LocalDate plannedArrival;

    @FutureOrPresent
    @Column(nullable = false)
    private LocalDate plannedDeparture;

    private LocalDateTime actualArrival;
    private LocalDateTime actualDeparture;

    @Column(nullable = false)
    private LocalDateTime createTime;

    @Column(nullable = false)
    private LocalDateTime lastUpdateTime;

    @ManyToOne
    @JoinColumn(name = "create_employee_id", nullable = false)
    private Employee createdBy;

    @ManyToOne
    @JoinColumn(name = "check_in_employee_id")
    private Employee checkedInBy;

    @ManyToOne
    @JoinColumn(name = "check_out_employee_id")
    private Employee checkedOutBy;

    @PrePersist
    private void setCreateTime(){
        createTime = LocalDateTime.now();
        lastUpdateTime = LocalDateTime.now();
    }
}
