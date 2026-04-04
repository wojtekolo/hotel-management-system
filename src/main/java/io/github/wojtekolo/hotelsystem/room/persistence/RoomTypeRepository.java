package io.github.wojtekolo.hotelsystem.room.persistence;

import io.github.wojtekolo.hotelsystem.room.model.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {
}
