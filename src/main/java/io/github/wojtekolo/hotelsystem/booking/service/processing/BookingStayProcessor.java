package io.github.wojtekolo.hotelsystem.booking.service.processing;

import io.github.wojtekolo.hotelsystem.booking.api.request.RoomStayCreateRequest;
import io.github.wojtekolo.hotelsystem.booking.exception.details.RoomStayViolationDetails;
import io.github.wojtekolo.hotelsystem.booking.api.request.RoomStayUpdateRequest;
import io.github.wojtekolo.hotelsystem.booking.exception.details.RoomStayViolationCode;
import io.github.wojtekolo.hotelsystem.booking.model.commands.RoomStayCreateCommand;
import io.github.wojtekolo.hotelsystem.booking.model.commands.RoomStayUpdateCommand;
import io.github.wojtekolo.hotelsystem.booking.model.entity.Booking;
import io.github.wojtekolo.hotelsystem.booking.model.violations.RoomStayViolation;
import io.github.wojtekolo.hotelsystem.employee.model.Employee;
import io.github.wojtekolo.hotelsystem.room.model.Room;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class BookingStayProcessor {
    private final BookingCommandMaker commandMaker;
    private final BookingErrorMapper errorMapper;

    public List<RoomStayViolationDetails> createBooking(Booking booking, List<RoomStayCreateRequest> requests, Employee employee, Map<Long, Room> rooms) {
        List<RoomStayCreateCommand> createCommands = requests.stream().map(commandMaker::fromCreateRequest).toList();
        return errorMapper.mapToErrorCodes(booking.addNewStays(createCommands, employee, rooms));
    }

    public List<RoomStayViolationDetails> updateBooking(Booking booking, List<RoomStayUpdateRequest> requests, Employee employee, Map<Long, Room> rooms) {
        List<Long> idsToKeep = requests.stream().map(RoomStayUpdateRequest::id).filter(Objects::nonNull).toList();

        List<RoomStayViolationDetails> idErrors = verifyStayIds(booking, requests);
        if (!idErrors.isEmpty()) return idErrors;

        List<RoomStayViolation> violations = new ArrayList<>();

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

        return errorMapper.mapToErrorCodes(violations);
    }

    private List<RoomStayViolationDetails> verifyStayIds(Booking booking, List<RoomStayUpdateRequest> requests){
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
}
