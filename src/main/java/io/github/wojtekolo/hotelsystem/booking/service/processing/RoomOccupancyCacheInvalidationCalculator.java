package io.github.wojtekolo.hotelsystem.booking.service.processing;

import io.github.wojtekolo.hotelsystem.booking.api.request.RoomStayUpdateRequest;
import io.github.wojtekolo.hotelsystem.booking.model.entity.Booking;
import io.github.wojtekolo.hotelsystem.booking.model.entity.RoomStay;
import io.github.wojtekolo.hotelsystem.booking.model.entity.RoomStayStatus;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class RoomOccupancyCacheInvalidationCalculator {
    public Set<Long> calculateAffectedRoomIds(Booking booking, List<RoomStayUpdateRequest> requests) {
        Set<Long> affectedRoomIds = new HashSet<>();

        Map<Long, RoomStayUpdateRequest> requestIds = requests.stream()
                                                              .filter(req -> req.id() != null)
                                                              .collect(Collectors.toMap(RoomStayUpdateRequest::id, req -> req));

//        Deleted stays
        affectedRoomIds.addAll(booking.getStays().stream()
                                      .filter(stay -> !requestIds.containsKey(stay.getId()))
                                      .filter(stay -> stay.getStatus() != RoomStayStatus.CANCELLED)
                                      .map(stay -> stay.getRoom().getId())
                                      .toList());

//        Updated stays
        affectedRoomIds.addAll(booking.getStays().stream()
                                      .filter(stay -> hasStayChanged(stay, requestIds.get(stay.getId())))
                                      .flatMap(stay -> Stream.of(
                                              stay.getRoom().getId(),
                                              requestIds.get(stay.getId()).roomId()
                                      ))
                                      .toList());

//        New stays
        affectedRoomIds.addAll(requests.stream()
                                       .filter(request -> request.id() == null)
                                       .map(RoomStayUpdateRequest::roomId)
                                       .toList());

        return affectedRoomIds;
    }

    private boolean hasStayChanged(RoomStay stay, RoomStayUpdateRequest request) {
        if (request == null) return false;
        return !Objects.equals(stay.getRoom().getId(), request.roomId()) ||
                !Objects.equals(stay.getActiveFrom(), request.from()) ||
                !Objects.equals(stay.getActiveTo(), request.to());
    }
}
