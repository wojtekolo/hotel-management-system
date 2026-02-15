package io.github.wojtekolo.hotelsystem.booking;

import io.github.wojtekolo.hotelsystem.employee.Employee;
import io.github.wojtekolo.hotelsystem.guest.Guest;
import io.github.wojtekolo.hotelsystem.room.Room;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
