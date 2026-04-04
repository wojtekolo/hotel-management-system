package io.github.wojtekolo.hotelsystem.room.persistence;

import io.github.wojtekolo.hotelsystem.room.api.RoomListItem;
import io.github.wojtekolo.hotelsystem.room.model.Room;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query("""
                SELECT new io.github.wojtekolo.hotelsystem.room.api.RoomListItem(
                r.id,
                r.name,
                rt.pricePerNight,
                r.floor,
                rt.name,
                r.operationalStatus,
                r.lifecycleStatus,
                rt.capacity
                )
                FROM Room r
                JOIN r.type rt
            """
    )
    Slice<RoomListItem> findAllRooms(Pageable pageable);

    boolean existsByName(String name);
}
