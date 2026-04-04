package io.github.wojtekolo.hotelsystem.customer.persistence;

import io.github.wojtekolo.hotelsystem.customer.model.LoyaltyStatus;
import io.github.wojtekolo.hotelsystem.customer.model.LoyaltyStatusName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoyaltyStatusRepository extends JpaRepository<LoyaltyStatus, Long> {
    Optional<LoyaltyStatus> findByName(LoyaltyStatusName name);
}
