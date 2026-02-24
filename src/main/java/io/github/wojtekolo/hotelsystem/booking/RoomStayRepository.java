package io.github.wojtekolo.hotelsystem.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoomStayRepository extends JpaRepository<RoomStay, Long> {

    default List<RoomStay> getConflicts(Long roomId, List<RoomStayStatus> statuses, LocalDate requestFrom, LocalDate requestTo){
        return getConflicts(roomId, statuses, requestFrom, requestTo, null);
    }

    @Query("""
            SELECT rs FROM RoomStay rs
            WHERE rs.room.id = ?1
            AND rs.status IN ?2
            AND (rs.activeTo>?3)
            AND (rs.activeFrom<?4)
            AND (?5 IS NULL OR rs.booking.id != ?5)
            """
    )
    List<RoomStay> getConflicts(Long roomId, List<RoomStayStatus> statuses, LocalDate requestFrom, LocalDate requestTo, Long excludedBookingID);
}
