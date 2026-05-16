package io.github.wojtekolo.hotelsystem.booking.service.processing;

import io.github.wojtekolo.hotelsystem.booking.api.request.RoomStayCreateRequest;
import io.github.wojtekolo.hotelsystem.booking.exception.details.RoomStayViolationDetails;
import io.github.wojtekolo.hotelsystem.booking.api.request.RoomStayUpdateRequest;
import io.github.wojtekolo.hotelsystem.booking.exception.details.RoomStayViolationCode;
import io.github.wojtekolo.hotelsystem.booking.model.commands.RoomStayCreateCommand;
import io.github.wojtekolo.hotelsystem.booking.model.commands.RoomStayUpdateCommand;
import io.github.wojtekolo.hotelsystem.booking.model.entity.Booking;
import io.github.wojtekolo.hotelsystem.booking.model.entity.RoomStayStatus;
import io.github.wojtekolo.hotelsystem.booking.model.violations.RoomStayViolation;
import io.github.wojtekolo.hotelsystem.employee.model.Employee;
import io.github.wojtekolo.hotelsystem.room.model.Room;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class BookingStayProcessor {
    private final BookingCommandMaker commandMaker;
    private final BookingErrorMapper errorMapper;

    public List<RoomStayViolationDetails> createBooking(Booking booking, List<RoomStayCreateRequest> requests, Employee employee, Map<Long, Room> rooms) {
        List<RoomStayCreateCommand> createCommands = requests.stream().map(commandMaker::fromCreateRequest).toList();
        return errorMapper.mapToErrorCodes(booking.addNewStays(createCommands, employee, rooms));
    }

    public BookingProcessingResult updateBooking(Booking booking, List<RoomStayUpdateRequest> requests, Employee employee, Map<Long, Room> rooms) {
        List<RoomStayViolationDetails> invalidIds = verifyStayIds(booking, requests);
        if (!invalidIds.isEmpty()) return new BookingProcessingResult(invalidIds, null);

        Set<Long> affectedRooms = calculateAffectedRoomIds(booking, requests);

        List<RoomStayViolation> violations = processBookingChanges(booking, requests, employee, rooms);
        return new BookingProcessingResult(errorMapper.mapToErrorCodes(violations), affectedRooms);
    }

    private List<RoomStayViolationDetails> verifyStayIds(Booking booking, List<RoomStayUpdateRequest> requests) {
        List<Long> existingIdsInBooking = booking.getStaysIds();

        List<Long> invalidIds = requests.stream()
                                        .map(RoomStayUpdateRequest::id)
                                        .filter(Objects::nonNull)
                                        .filter(id -> !existingIdsInBooking.contains(id))
                                        .toList();
        return invalidIds.stream()
                         .map(id -> new RoomStayViolationDetails(id, null, RoomStayViolationCode.STAY_NOT_FOUND_IN_BOOKING, null))
                         .toList();
    }

    private Set<Long> calculateAffectedRoomIds(Booking booking, List<RoomStayUpdateRequest> requests) {
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
                                      .filter(stay -> requestIds.containsKey(stay.getId()))
                                      .filter(stay -> {
                                          RoomStayUpdateRequest request = requestIds.get(stay.getId());
                                          return !Objects.equals(stay.getRoom().getId(), request.roomId()) ||
                                                  !Objects.equals(stay.getActiveFrom(), request.from()) ||
                                                  !Objects.equals(stay.getActiveTo(), request.to());
                                      })
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

    private List<RoomStayViolation> processBookingChanges(Booking booking, List<RoomStayUpdateRequest> requests, Employee employee, Map<Long, Room> rooms) {
        List<RoomStayViolation> violations = new ArrayList<>();
        List<Long> idsToKeep = requests.stream().map(RoomStayUpdateRequest::id).filter(Objects::nonNull).toList();

//        Delete
        violations.addAll(booking.deleteStaysExceptFor(idsToKeep));

//        Update existing
        List<RoomStayUpdateCommand> updateCommands = requests
                .stream()
                .filter(req -> req.id() != null && req.id() >= 0)
                .map(commandMaker::prepareUpdateCommand)
                .toList();
        violations.addAll(booking.updateCurrentStays(updateCommands, rooms));

//        Create new
        List<RoomStayCreateCommand> createCommands = requests
                .stream()
                .filter(req -> req.id() == null)
                .map(commandMaker::prepareCreateCommand).toList();
        violations.addAll(booking.addNewStays(createCommands, employee, rooms));

        return violations;
    }
}
