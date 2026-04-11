package io.github.wojtekolo.hotelsystem.room.persistence;

import io.github.wojtekolo.hotelsystem.room.api.RoomListItem;
import io.github.wojtekolo.hotelsystem.room.model.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Room r where r.id in :ids")
    List<Room> findAllByIdWithLock(List<Long> ids);
}
