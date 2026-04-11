package io.github.wojtekolo.hotelsystem.booking.service.validation;

import io.github.wojtekolo.hotelsystem.booking.exception.details.RoomStayExternalConflict;
import io.github.wojtekolo.hotelsystem.booking.exception.details.ConflictingStay;
import io.github.wojtekolo.hotelsystem.booking.model.entity.RoomStay;
import io.github.wojtekolo.hotelsystem.booking.model.entity.RoomStayStatus;
import io.github.wojtekolo.hotelsystem.booking.persistence.RoomStayRepository;
import io.github.wojtekolo.hotelsystem.booking.service.BookingMapper;
import io.github.wojtekolo.hotelsystem.room.model.Room;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BookingExternalValidator {

    private final RoomStayRepository roomStayRepository;
    private final BookingMapper bookingMapper;

    public List<RoomStayExternalConflict> validate(List<RoomStay> stays, Long bookingId) {
        List<RoomStayExternalConflict> allConflicts = new ArrayList<>();
        for (RoomStay stay : stays) {
            if (!stay.doesCollide()) continue;
            Room room = stay.getRoom();

            List<RoomStay> conflicts = roomStayRepository.getConflicts(room.getId(), List.of(RoomStayStatus.ACTIVE,
                    RoomStayStatus.PLANNED), stay.getActiveFrom(), stay.getActiveTo(), bookingId);

            if (!conflicts.isEmpty()) {
                List<ConflictingStay> details = new ArrayList<>();
                for (RoomStay conflict : conflicts) {
                    details.add(bookingMapper.toRoomStayConflictDetails(conflict));
                }
                allConflicts.add(new RoomStayExternalConflict(
                        room.getId(), room.getName(), stay.getId(), stay.getActiveFrom(), stay.getActiveTo(), details
                ));
            }
        }
        return allConflicts;
    }
}
