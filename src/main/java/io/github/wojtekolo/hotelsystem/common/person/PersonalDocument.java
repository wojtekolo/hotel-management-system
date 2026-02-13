package io.github.wojtekolo.hotelsystem.common.person;

import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class PersonalDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType type;

    @Column(nullable = false, unique = true)
    private String number;

    @ManyToOne
    @JoinColumn(name = "person_id")
    private Person person;
}
