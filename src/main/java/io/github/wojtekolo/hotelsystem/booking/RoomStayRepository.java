package io.github.wojtekolo.hotelsystem.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface RoomStayRepository extends JpaRepository<RoomStay, Long> {

    @Query("""
            SELECT rs FROM RoomStay rs
            WHERE rs.room.id = ?1
            AND rs.status IN ?2
            AND (rs.activeTo>?3)
            AND (rs.activeFrom<?4)
            """
    )
    List<RoomStay> getConficts(Long roomId, List<RoomStayStatus> statuses, LocalDate requestFrom, LocalDate requestTo);
}
