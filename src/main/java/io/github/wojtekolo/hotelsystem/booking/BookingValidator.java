package io.github.wojtekolo.hotelsystem.booking;

import io.github.wojtekolo.hotelsystem.common.exceptions.BookingRequestConflictException;
import io.github.wojtekolo.hotelsystem.common.exceptions.RoomStayStatusException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Component
public class BookingValidator {

    public void validateInternalConflicts(List<RoomStay> stays) {
        List<InternalRoomStayConflict> conflicts = new ArrayList<>();

        List<RoomStay> staysCopy = new ArrayList<>(stays);
        staysCopy.sort(Comparator
                .comparing((RoomStay rs) -> rs.getRoom().getId())
                .thenComparing(RoomStay::getActiveFrom)
        );

        for (int i = 0; i < staysCopy.size() - 1; i++) {
            for (int j = i + 1; j < staysCopy.size(); j++) {
                if (!Objects.equals(staysCopy.get(i).getRoom().getId(), staysCopy.get(j).getRoom().getId())) break;

                if (doStaysOverLap(staysCopy.get(i), staysCopy.get(j)) && Objects.equals(staysCopy.get(i).getRoom()
                                                                                                  .getId(), staysCopy.get(j)
                                                                                                                     .getRoom()
                                                                                                                     .getId())) {
                    conflicts.add(new InternalRoomStayConflict(
                            staysCopy.get(i).getRoom().getId(),
                            staysCopy.get(i).getActiveFrom(),
                            staysCopy.get(i).getActiveTo(),
                            staysCopy.get(j).getActiveFrom(),
                            staysCopy.get(j).getActiveTo()
                    ));
                }
            }
        }
        if (!conflicts.isEmpty()) throw new BookingRequestConflictException(conflicts);
    }

    public void validateUpdatedRoomStays(Booking newBooking, List<RoomStay> currentStays, List<RoomStayUpdateRequest> newStays){
        List<Long> finalIds = newStays.stream()
                                      .map(RoomStayUpdateRequest::id)
                                      .toList();

        List<RoomStayBadStatusDetails> details = new ArrayList<>();

        currentStays.stream()
                    .filter(roomStay -> !finalIds.contains(roomStay.getId()))
                    .forEach(roomStay -> {
                        if (roomStay.getStatus() != RoomStayStatus.PLANNED && roomStay.getStatus() != RoomStayStatus.CANCELLED)
                            details.add(new RoomStayBadStatusDetails(roomStay.getId(),roomStay.getStatus(), RoomStayErrorCode.ONLY_PLANNED_STAY_CAN_BE_CANCELLED));
                        else {
                            roomStay.setStatus(RoomStayStatus.CANCELLED);
                            newBooking.addStay(roomStay);
                        }
                    });

        if (!details.isEmpty())
            throw new RoomStayStatusException(
                    "Error updating room stays",
                    newBooking.getId(),
                    details
            );
    }

    private boolean doStaysOverLap(RoomStay request1, RoomStay request2) {
        return request1.getActiveTo().isAfter(request2.getActiveFrom()) && request1.getActiveFrom()
                                                                                   .isBefore(request2.getActiveTo());
    }
}
