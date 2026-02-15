package io.github.wojtekolo.hotelsystem.guest;

import io.github.wojtekolo.hotelsystem.common.person.Person;
import jakarta.persistence.*;

@Entity
public class Guest {
    @Id
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @MapsId
    @JoinColumn(name = "id")
    Person person;

    private String description;

    private boolean hasBreakfast = false;
}
