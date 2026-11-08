package io.github.wojtekolo.hotelsystem.booking.model.entity;

import io.github.wojtekolo.hotelsystem.booking.exception.details.BookingErrorCode;
import io.github.wojtekolo.hotelsystem.booking.exception.BookingStatusException;
import io.github.wojtekolo.hotelsystem.booking.model.commands.RoomStayCreateCommand;
import io.github.wojtekolo.hotelsystem.booking.model.commands.RoomStayUpdateCommand;
import io.github.wojtekolo.hotelsystem.booking.model.violations.RoomStayViolation;
import io.github.wojtekolo.hotelsystem.booking.model.violations.RoomStayViolationReason;
import io.github.wojtekolo.hotelsystem.customer.model.Customer;
import io.github.wojtekolo.hotelsystem.customer.model.LoyaltyStatus;
import io.github.wojtekolo.hotelsystem.employee.model.Employee;
import io.github.wojtekolo.hotelsystem.room.model.Room;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
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
    @JoinColumn(name = "create_employee_id", nullable = true)
    private Employee createBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @PrePersist
    private void setCreateTime() {
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

    public static Booking createDefault(Customer customer) {
        return Booking.builder()
                      .customer(customer)
                      .status(BookingStatus.PLANNED)
                      .stays(new ArrayList<>())
                      .build();
    }

    public void ensureEditable() {
        if (getStatus() == BookingStatus.CANCELLED || getStatus() == BookingStatus.COMPLETED || getStatus() == BookingStatus.NOSHOW)
            throw new BookingStatusException("Cannot edit cancelled booking",
                    BookingErrorCode.EDIT_INVALID_STATUS, getId());
    }

    public List<RoomStayViolation> deleteStaysExceptFor(List<Long> idsToLeave) {
        return getStays().stream()
                         .filter(stay -> !idsToLeave.contains(stay.getId()))
                         .flatMap(stay ->
                                 stay.tryCancel().map(reason ->
                                         new RoomStayViolation(stay.getId(), stay.getStatus(), reason, null)).stream())
                         .toList();
    }

    public List<RoomStayViolation> updateCurrentStays(List<RoomStayUpdateCommand> commands, Map<Long, Room> rooms) {
        Map<Long, RoomStay> currentStaysMap = getStays().stream().collect(Collectors.toMap(RoomStay::getId, s -> s));
        List<RoomStayViolation> allViolations = new ArrayList<>();

        for (RoomStayUpdateCommand command : commands) {
            if (command.stayId() == null) continue;
            RoomStay roomStay = currentStaysMap.get(command.stayId());
            if (roomStay == null) continue;

            allViolations.addAll(validateStayInvariants(roomStay.getId(), roomStay.getStatus(), command.roomId(), command.from(), command.to()));

            Room room = rooms.get(command.roomId());
            if (room != null) {
                allViolations.add(updateRoom(roomStay, room));
                allViolations.addAll(updateDates(roomStay, command.from(), command.to()));
            } else {
                if (!roomStay.canEditStartDate() && !command.from().equals(roomStay.getActiveFrom())) {
                    allViolations.add(new RoomStayViolation(roomStay.getId(), roomStay.getStatus(), RoomStayViolationReason.START_DATE_EDIT_INVALID_STATUS, null));
                }
                if (!roomStay.canEditEndDate() && !command.to().equals(roomStay.getActiveTo())) {
                    allViolations.add(new RoomStayViolation(roomStay.getId(), roomStay.getStatus(), RoomStayViolationReason.END_DATE_EDIT_INVALID_STATUS, null));
                }
            }

        }
        return allViolations.stream().filter(Objects::nonNull).toList();
    }

    public List<RoomStayViolation> addNewStays(List<RoomStayCreateCommand> commands, Map<Long, Room> rooms) {
        List<RoomStayViolation> allViolations = new ArrayList<>();

        for (RoomStayCreateCommand command : commands) {
            allViolations.addAll(validateStayInvariants(null, null, command.roomId(), command.from(), command.to()));

            Room room = rooms.get(command.roomId());
            if (room != null) {
                createAndAddPlannedStay(room, command.from(), command.to());
            }
        }
        return allViolations;
    }

    public List<Long> getStaysIds() {
        return stays.stream().map(RoomStay::getId).toList();
    }

    private RoomStayViolation updateRoom(RoomStay roomStay, Room newRoom) {
        Optional<RoomStayViolationReason> reason = roomStay.tryUpdateRoom(newRoom);
        return reason.map(stayUpdateViolationReason ->
                             new RoomStayViolation(roomStay.getId(), roomStay.getStatus(), stayUpdateViolationReason, null))
                     .orElse(null);
    }

    private List<RoomStayViolation> updateDates(RoomStay roomStay, LocalDate newFrom, LocalDate newTo) {
        List<RoomStayViolationReason> reasons = roomStay.tryUpdateDates(newFrom, newTo);
        return reasons
                .stream()
                .map(
                        stayUpdateViolationReason ->
                                new RoomStayViolation(roomStay.getId(), roomStay.getStatus(), stayUpdateViolationReason, null)
                )
                .toList();
    }

    private void createAndAddPlannedStay(Room room, LocalDate from, LocalDate to) {
        addStay(RoomStay.createPlanned(
                this,
                room,
                getSafeDiscount(),
                from,
                to
        ));
    }

    private BigDecimal getSafeDiscount() {
        return Optional.ofNullable(customer)
                       .map(Customer::getLoyaltyStatus)
                       .map(LoyaltyStatus::getDiscount)
                       .orElse(BigDecimal.ZERO);
    }

    private Map<String, Object> getContext(RoomStayViolationReason reason, Long roomId, LocalDate from, LocalDate to) {
        Map<String, Object> context = new HashMap<>();
        context.put("roomId", roomId);

        switch (reason) {
            case END_DATE_NOT_AFTER_START_DATE -> {
                context.put("from", from);
                context.put("to", to);
            }
            case START_DATE_IN_THE_PAST -> context.put("from", from);
            case END_DATE_IN_THE_PAST -> context.put("to", to);
        }
        return context;
    }

    private List<RoomStayViolation> validateStayInvariants(Long stayId, RoomStayStatus status, Long roomId, LocalDate from, LocalDate to) {
        List<RoomStayViolationReason> reasons = new ArrayList<>(RoomStay.validateNewStayDates(from, to));

        return reasons.stream()
                      .map(reason -> new RoomStayViolation(stayId, status, reason, getContext(reason, roomId, from, to)))
                      .collect(Collectors.toList());
    }
}
