package io.github.wojtekolo.hotelsystem.room;

import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private Integer floor;
    private String description;

    @ManyToOne
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "lifecycle_status")
    private LifecycleStatus lifecycleStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "operational_status")
    private OperationalStatus operationalStatus;
}
