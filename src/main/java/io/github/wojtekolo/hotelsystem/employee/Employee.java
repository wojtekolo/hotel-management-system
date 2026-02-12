package io.github.wojtekolo.hotelsystem.employee;

import io.github.wojtekolo.hotelsystem.common.person.Person;
import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class Employee {
    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    Person person;

    private String description;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private EmployeeRole employeeRole;
}
