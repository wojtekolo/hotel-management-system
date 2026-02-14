package io.github.wojtekolo.hotelsystem.customer;

import io.github.wojtekolo.hotelsystem.common.person.Person;
import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class Customer {
    @Id
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @MapsId
    @JoinColumn(name = "id")
    Person person;

    private String description;

    @ManyToOne
    @JoinColumn(name = "loyalty_status_id")
    private LoyaltyStatus loyaltyStatus;
}
