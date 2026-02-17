package io.github.wojtekolo.hotelsystem.customer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoyaltyStatusRepository extends JpaRepository<LoyaltyStatus, Long> {
    Optional<LoyaltyStatus> findByName(LoyaltyStatusName name);
}
