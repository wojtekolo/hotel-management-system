package io.github.wojtekolo.hotelsystem.guest;

import io.github.wojtekolo.hotelsystem.common.person.Person;
import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class Guest {
    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    Person person;

    private String description;

    @ManyToOne
    @JoinColumn(name = "loyalty_status_id")
    private LoyaltyStatus loyaltyStatus;
}
