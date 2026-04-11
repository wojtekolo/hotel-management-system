package io.github.wojtekolo.hotelsystem.booking.service.validation;

import io.github.wojtekolo.hotelsystem.booking.exception.details.RoomStayInternalConflict;
import io.github.wojtekolo.hotelsystem.booking.model.entity.RoomStay;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Component
public class BookingInternalValidator {

    public List<RoomStayInternalConflict> validate(List<RoomStay> stays) {
        List<RoomStayInternalConflict> conflicts = new ArrayList<>();

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

                if (doStaysOverLap(staysCopy.get(i), staysCopy.get(j))) {
                    conflicts.add(new RoomStayInternalConflict(
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
