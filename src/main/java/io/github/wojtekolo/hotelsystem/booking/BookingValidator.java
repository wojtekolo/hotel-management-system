package io.github.wojtekolo.hotelsystem.booking;

import io.github.wojtekolo.hotelsystem.room.Room;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class BookingValidator {

    private final RoomStayRepository roomStayRepository;
    private final BookingMapper bookingMapper;

    public List<ExternalRoomStayConflict> validateExternalConflicts(List<RoomStay> stays, Long bookingId) {
        List<ExternalRoomStayConflict> allConflicts = new ArrayList<>();
        for (RoomStay stay : stays) {
            Room room = stay.getRoom();

            List<RoomStay> conflicts = roomStayRepository.getConflicts(room.getId(), List.of(RoomStayStatus.ACTIVE,
                    RoomStayStatus.PLANNED), stay.getActiveFrom(), stay.getActiveTo(), bookingId);

            if (!conflicts.isEmpty()) {
                List<RoomStayConflictDetails> details = new ArrayList<>();
                for (RoomStay conflict : conflicts) {
                    details.add(bookingMapper.toRoomStayConflictDetails(conflict));
                }
                allConflicts.add(new ExternalRoomStayConflict(
                        room.getId(), room.getName(), stay.getId(), stay.getActiveFrom(), stay.getActiveTo(), details
                ));
            }
        }
        return allConflicts;
    }

    public List<InternalRoomStayConflict> validateInternalConflicts(List<RoomStay> stays) {
        List<InternalRoomStayConflict> conflicts = new ArrayList<>();

        List<RoomStay> staysCopy = new ArrayList<>(stays);
        staysCopy.sort(Comparator
                .comparing((RoomStay rs) -> rs.getRoom().getId())
                .thenComparing(RoomStay::getActiveFrom)
        );

        for (int i = 0; i < staysCopy.size() - 1; i++) {
            if (!staysCopy.get(i).doesCollide()) continue;
            for (int j = i + 1; j < staysCopy.size(); j++) {
                if (!staysCopy.get(j).doesCollide()) continue;
                if (!Objects.equals(staysCopy.get(i).getRoom().getId(), staysCopy.get(j).getRoom().getId())) break;

                if (doStaysOverLap(staysCopy.get(i), staysCopy.get(j)) &&
                        Objects.equals(
                                staysCopy.get(i).getRoom().getId(),
                                staysCopy.get(j).getRoom().getId()
                        )) {
                    conflicts.add(new InternalRoomStayConflict(
                            staysCopy.get(i).getRoom().getId(),
                            staysCopy.get(i).getId(),
                            staysCopy.get(i).getActiveFrom(),
                            staysCopy.get(i).getActiveTo(),
                            staysCopy.get(j).getId(),
                            staysCopy.get(j).getActiveFrom(),
                            staysCopy.get(j).getActiveTo()
                    ));
                }
            }
        }
        return conflicts;
    }

    private boolean doStaysOverLap(RoomStay request1, RoomStay request2) {
        return request1.getActiveTo().isAfter(request2.getActiveFrom()) && request1.getActiveFrom()
                                                                                   .isBefore(request2.getActiveTo());
    }
}
