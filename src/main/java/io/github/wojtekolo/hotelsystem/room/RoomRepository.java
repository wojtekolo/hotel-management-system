package io.github.wojtekolo.hotelsystem.room;

import io.github.wojtekolo.hotelsystem.room.dtos.RoomListItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query("""
        SELECT new io.github.wojtekolo.hotelsystem.room.dtos.RoomListItem(
        r.id,
        r.name,
        rt.pricePerNight,
        r.floor,
        rt.name,
        rs.name,
        rt.capacity
        )
        FROM Room r
        JOIN r.type rt
        JOIN r.status rs
    """
    )
    Slice<RoomListItem> findAllRooms(Pageable pageable);
}
