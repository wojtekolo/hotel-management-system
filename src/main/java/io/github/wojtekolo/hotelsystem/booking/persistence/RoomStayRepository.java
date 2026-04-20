package io.github.wojtekolo.hotelsystem.booking.persistence;

import io.github.wojtekolo.hotelsystem.booking.api.response.OccupiedRange;
import io.github.wojtekolo.hotelsystem.booking.model.entity.RoomStay;
import io.github.wojtekolo.hotelsystem.booking.model.entity.RoomStayStatus;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Repository
public interface RoomStayRepository extends JpaRepository<RoomStay, Long> {

    default List<RoomStay> getConflicts(Long roomId, List<RoomStayStatus> statuses, LocalDate requestFrom, LocalDate requestTo) {
        return getConflicts(roomId, statuses, requestFrom, requestTo, null);
    }

    @QueryHints(value = {
            @QueryHint(name = org.hibernate.jpa.HibernateHints.HINT_FLUSH_MODE, value = "COMMIT")
    })
    @Query("""
            SELECT rs FROM RoomStay rs
            WHERE rs.room.id = :roomId
            AND rs.status IN :statuses
            AND (rs.activeTo>:requestFrom)
            AND (rs.activeFrom<:requestTo)
            AND (:excludedBookingId IS NULL OR rs.booking.id != :excludedBookingId)
            """
    )
    List<RoomStay> getConflicts(Long roomId, List<RoomStayStatus> statuses,
                                LocalDate requestFrom, LocalDate requestTo, Long excludedBookingId);

    long countByRoomId(Long roomId);

    default List<OccupiedRange> getOccupiedRangesForRoom(Long roomId, Set<RoomStayStatus> statuses,
                                                         LocalDate from, LocalDate to) {
        return getOccupiedRangesForRoom(roomId, statuses, from, to, null);
    }


    @Query("""
                SELECT NEW io.github.wojtekolo.hotelsystem.booking.api.response.OccupiedRange(rs.activeFrom, rs.activeTo, rs.booking.id)
                FROM RoomStay rs
                WHERE rs.room.id = :roomId
                AND rs.status IN :statuses
                AND (rs.activeTo > :from)
                AND (rs.activeFrom < :to)
                AND (:excludedBookingId IS NULL OR rs.booking.id <> :excludedBookingId)
            """)
    List<OccupiedRange> getOccupiedRangesForRoom(
            Long roomId,
            Set<RoomStayStatus> statuses,
            LocalDate from,
            LocalDate to,
            Long excludedBookingId);
}
