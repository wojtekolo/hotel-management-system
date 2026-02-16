package io.github.wojtekolo.hotelsystem.guest;

import io.github.wojtekolo.hotelsystem.common.person.Person;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class Guest {
    @Id
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @MapsId
    @JoinColumn(name = "id")
    private Person person;

    private String description;

    private boolean hasBreakfast = false;
}
