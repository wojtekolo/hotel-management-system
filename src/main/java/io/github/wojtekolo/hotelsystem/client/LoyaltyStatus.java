package io.github.wojtekolo.hotelsystem.client;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
public class LoyaltyStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal discount;
}
