package io.github.wojtekolo.hotelsystem.booking;

import io.github.wojtekolo.hotelsystem.customer.Customer;
import io.github.wojtekolo.hotelsystem.employee.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false)
    private LocalDateTime createTime;

    @ManyToOne
    @JoinColumn(name = "create_employee_id", nullable = false)
    private Employee createBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @PrePersist
    private void setCreateTime(){
        createTime = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    private List<RoomStay> stays;

    public void addStay(RoomStay stay) {
        this.stays.add(stay);
        stay.setBooking(this);
    }

    public BigDecimal calculateTotalCost() {
        BigDecimal total = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        for (RoomStay roomStay : stays) {
            if (roomStay.countsTowardTotal()) total = total.add(roomStay.calculateTotalCost());
        }
        return total;
    }

    public static Booking createDefault(Customer customer, Employee employee) {
        return Booking.builder()
                      .customer(customer)
                      .createBy(employee)
                      .paymentStatus(PaymentStatus.UNPAID)
                      .status(BookingStatus.PLANNED)
                      .stays(new ArrayList<>())
                      .build();
    }
}
