package io.github.wojtekolo.hotelsystem.booking;

import io.github.wojtekolo.hotelsystem.employee.Employee;
import io.github.wojtekolo.hotelsystem.guest.Guest;
import io.github.wojtekolo.hotelsystem.room.Room;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class RoomStay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    Booking booking;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private BigDecimal pricePerNight;

    @Column(nullable = false)
    private LocalDate activeFrom;

    @Column(nullable = false)
    private LocalDate activeTo;

    private LocalDateTime actualCheckIn;
    private LocalDateTime actualCheckOut;

    @Column(nullable = false)
    private LocalDateTime createTime;

    @Column(nullable = false)
    private LocalDateTime lastUpdateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomStayStatus status;

    @ManyToMany
    @JoinTable(
            name = "stay_guest",
            joinColumns = @JoinColumn(name = "stay_id"),
            inverseJoinColumns = @JoinColumn(name = "guest_id")
    )
    private List<Guest> guests;

    @ManyToOne
    @JoinColumn(name = "create_employee_id", nullable = false)
    private Employee createBy;

    @ManyToOne
    @JoinColumn(name = "check_in_employee_id")
    private Employee checkInBy;

    @ManyToOne
    @JoinColumn(name = "check_out_employee_id")
    private Employee checkOutBy;

    @PrePersist
    private void setCreateTime() {
        createTime = LocalDateTime.now();
        lastUpdateTime = LocalDateTime.now();
    }

    @PreUpdate
    private void setUpdateTime() {
        this.lastUpdateTime = LocalDateTime.now();
    }

    public BigDecimal calculateTotalCost() {
        long days = ChronoUnit.DAYS.between(activeFrom, activeTo);
        return pricePerNight.multiply(BigDecimal.valueOf(days));
    }

    public boolean canBeCancelled() {
        return status == RoomStayStatus.CANCELLED || status == RoomStayStatus.PLANNED;
    }

    public boolean canEditPrice() {
        return status == RoomStayStatus.PLANNED;
    }

    public boolean canEditStartDate() {
        return status == RoomStayStatus.PLANNED;
    }

    public boolean canEditEndDate() {
        return status == RoomStayStatus.PLANNED || status == RoomStayStatus.ACTIVE;
    }

    public boolean doesCollide(){
        return status == RoomStayStatus.PLANNED || status == RoomStayStatus.ACTIVE;
    }

    public boolean canEditRoom() {
        return status == RoomStayStatus.PLANNED;
    }

    public static RoomStay create(Booking booking, Room room, BigDecimal discount, Employee employee, LocalDate from, LocalDate to, BigDecimal customPricePerNight, RoomStayStatus status) {
        return RoomStay.builder()
                       .booking(booking)
                       .room(room)
                       .pricePerNight(calculatePricePerNight(room, discount, customPricePerNight))
                       .activeFrom(from)
                       .activeTo(to)
                       .status(status)
                       .createBy(employee)
                       .build();
    }

    public boolean tryCancel() {
        if (canBeCancelled()) {
            this.status = RoomStayStatus.CANCELLED;
            return true;
        }
        return false;
    }

    public boolean tryUpdateActiveFrom(LocalDate newActiveFrom) {
        if (canEditStartDate() || newActiveFrom.equals(this.activeFrom)) {
            this.activeFrom = newActiveFrom;
            return true;
        }
        return false;
    }

    public boolean tryUpdateActiveTo(LocalDate newActiveTo) {
        if (canEditEndDate() || newActiveTo.equals(this.activeTo)) {
            this.activeTo = newActiveTo;
            return true;
        }
        return false;
    }

    public boolean tryEditPrice(BigDecimal newPricePerNight) {
        if (newPricePerNight == null) return true;
        if (canEditPrice() || newPricePerNight.equals(this.pricePerNight)) {
            this.pricePerNight = newPricePerNight;
            return true;
        }
        return false;
    }

    public boolean tryUpdateRoom(Room room) {
        if (canEditRoom() || room.getId().equals(this.room.getId())) {
            this.room = room;
            return true;
        }
        return false;
    }
    private static BigDecimal calculatePricePerNight(Room room, BigDecimal discount, BigDecimal customPricePerNight) {
        if (customPricePerNight == null) {
            return room.getType().getPricePerNight()
                       .multiply(BigDecimal.ONE.subtract(discount));
        } else {
            return customPricePerNight;
        }
    }
}
