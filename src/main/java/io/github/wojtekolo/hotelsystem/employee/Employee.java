package io.github.wojtekolo.hotelsystem.employee;

import io.github.wojtekolo.hotelsystem.common.person.Person;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class Employee {
    @Id
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @MapsId
    @JoinColumn(name = "id")
    Person person;

    private String description;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private EmployeeRole employeeRole;

    @Column(nullable = false, unique = true, length = 11)
    private String pesel;

    @Column(nullable = false, unique = true)
    private String idCardNumber;
    @Email
    @Column(nullable = false, unique = true)
    private String workEmail;

    @Column(nullable = false, unique = true)
    private String workPhone;

    @Column(unique = true)
    private String emergencyPhone;
}
