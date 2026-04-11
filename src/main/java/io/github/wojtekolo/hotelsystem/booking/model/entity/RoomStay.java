package io.github.wojtekolo.hotelsystem.booking.model.entity;

import io.github.wojtekolo.hotelsystem.booking.model.violations.RoomStayViolationReason;
import io.github.wojtekolo.hotelsystem.employee.model.Employee;
import io.github.wojtekolo.hotelsystem.guest.Guest;
import io.github.wojtekolo.hotelsystem.room.model.Room;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class RoomStay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    @Setter
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

    public static RoomStay createPlanned(Booking booking, Room room, BigDecimal discount, Employee employee, LocalDate from, LocalDate to, BigDecimal customPricePerNight) {
        return create(booking, room, discount, employee, from, to, customPricePerNight, RoomStayStatus.PLANNED);
    }

    private static RoomStay create(Booking booking, Room room, BigDecimal discount, Employee employee, LocalDate from, LocalDate to, BigDecimal customPricePerNight, RoomStayStatus status) {
        return RoomStay.builder()
                       .booking(booking)
                       .room(room)
                       .pricePerNight(calculatePricePerNight(room.getType()
                                                                 .getPricePerNight(), discount, customPricePerNight))
                       .activeFrom(from)
                       .activeTo(to)
                       .status(status)
                       .createBy(employee)
                       .build();
    }

    public BigDecimal calculateTotalCost() {
        if (activeFrom.isAfter(activeTo)) return BigDecimal.ZERO;
        long days = ChronoUnit.DAYS.between(activeFrom, activeTo);
        return pricePerNight.multiply(BigDecimal.valueOf(days));
    }

    boolean canBeCancelled() {
        return status == RoomStayStatus.CANCELLED || status == RoomStayStatus.PLANNED;
    }

    boolean canEditPrice() {
        return status == RoomStayStatus.PLANNED;
    }

    boolean canEditStartDate() {
        return status == RoomStayStatus.PLANNED;
    }

    boolean canEditEndDate() {
        return status == RoomStayStatus.PLANNED || status == RoomStayStatus.ACTIVE;
    }

    public boolean doesCollide() {
        return status == RoomStayStatus.PLANNED || status == RoomStayStatus.ACTIVE;
    }

    public boolean countsTowardTotal() {
        return EnumSet.of(RoomStayStatus.PLANNED, RoomStayStatus.ACTIVE, RoomStayStatus.COMPLETED).contains(status);
    }

    boolean canEditRoom() {
        return status == RoomStayStatus.PLANNED;
    }

    Optional<RoomStayViolationReason> tryCancel() {
        if (canBeCancelled()) {
            this.status = RoomStayStatus.CANCELLED;
            return Optional.empty();
        }
        return Optional.of(RoomStayViolationReason.CANCEL_INVALID_STATUS);
    }

    Optional<RoomStayViolationReason> tryUpdateActiveFrom(LocalDate newActiveFrom) {
        if (canEditStartDate() || newActiveFrom.equals(this.activeFrom)) {
            this.activeFrom = newActiveFrom;
            return Optional.empty();
        }
        return Optional.of(RoomStayViolationReason.START_DATE_EDIT_INVALID_STATUS);
    }

    Optional<RoomStayViolationReason> tryUpdateActiveTo(LocalDate newActiveTo) {
        if (canEditEndDate() || newActiveTo.equals(this.activeTo)) {
            this.activeTo = newActiveTo;
            return Optional.empty();
        }
        return Optional.of(RoomStayViolationReason.END_DATE_EDIT_INVALID_STATUS);
    }

    List<RoomStayViolationReason> tryUpdateDates(LocalDate newActiveFrom, LocalDate newActiveTo) {
        List<RoomStayViolationReason> violationReasons = new ArrayList<>();

        if (!(canEditStartDate() || newActiveFrom.equals(this.activeFrom)))
            violationReasons.add(RoomStayViolationReason.START_DATE_EDIT_INVALID_STATUS);
        if (!(canEditEndDate() || newActiveTo.equals(this.activeTo)))
            violationReasons.add(RoomStayViolationReason.END_DATE_EDIT_INVALID_STATUS);

        if (violationReasons.isEmpty()) {
            this.activeTo = newActiveTo;
            this.activeFrom = newActiveFrom;
        }

        return violationReasons;
    }

    Optional<RoomStayViolationReason> tryUpdatePrice(BigDecimal newPricePerNight) {
        if (newPricePerNight == null) return Optional.empty();
        if (newPricePerNight.compareTo(BigDecimal.ZERO) < 0) {
            this.pricePerNight = BigDecimal.ZERO;
            return Optional.empty();
        }
        if (canEditPrice() || newPricePerNight.compareTo(this.pricePerNight) == 0) {
            this.pricePerNight = newPricePerNight;
            return Optional.empty();
        }
        return Optional.of(RoomStayViolationReason.PRICE_EDIT_INVALID_STATUS);
    }

    Optional<RoomStayViolationReason> tryUpdateRoom(Room room) {
        if (canEditRoom() || room.getId().equals(this.room.getId())) {
            this.room = room;
            return Optional.empty();
        }
        return Optional.of(RoomStayViolationReason.ROOM_EDIT_INVALID_STATUS);
    }

    static BigDecimal calculatePricePerNight(BigDecimal roomPricePerNight, BigDecimal discount, BigDecimal customPricePerNight) {
        if (discount == null) {
            throw new IllegalArgumentException("Discount cannot be null");
        }
        if (discount.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Discount must be greater than or equal 0");
        if (discount.compareTo(BigDecimal.valueOf(1)) > 0)
            throw new IllegalArgumentException("Discount must be less than or equal to 1");


        if (customPricePerNight == null) {
            return roomPricePerNight
                    .multiply(BigDecimal.ONE.subtract(discount)).setScale(2, RoundingMode.HALF_UP);
        } else {
//            if (customPricePerNight.compareTo(BigDecimal.ZERO) < 0)
//                throw new IllegalArgumentException("Price cannot be negative");
            return customPricePerNight.setScale(2, RoundingMode.HALF_UP);
        }
    }

    public static List<RoomStayViolationReason> validateNewStayDates(LocalDate from, LocalDate to) {
        List<RoomStayViolationReason> violationReasons = new ArrayList<>();
        if (!to.isAfter(from)) {
            violationReasons.add(RoomStayViolationReason.END_DATE_NOT_AFTER_START_DATE);
        }
        if (from.isBefore(LocalDate.now())) {
            violationReasons.add(RoomStayViolationReason.START_DATE_IN_THE_PAST);
        }
        if (to.isBefore(LocalDate.now())) {
            violationReasons.add(RoomStayViolationReason.END_DATE_IN_THE_PAST);
        }
        return violationReasons;
    }

    public static List<RoomStayViolationReason> validateNewPrice(BigDecimal price) {
        List<RoomStayViolationReason> violationReasons = new ArrayList<>();
        if (price == null) return violationReasons;
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            violationReasons.add(RoomStayViolationReason.PRICE_NEGATIVE);
        }
        return violationReasons;
    }
}
