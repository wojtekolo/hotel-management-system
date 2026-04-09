package io.github.wojtekolo.hotelsystem.booking.service;

import io.github.wojtekolo.hotelsystem.booking.persistence.BookingRepository;
import io.github.wojtekolo.hotelsystem.booking.api.*;
import io.github.wojtekolo.hotelsystem.booking.exception.BookingErrorCode;
import io.github.wojtekolo.hotelsystem.booking.exception.BookingStatusException;
import io.github.wojtekolo.hotelsystem.booking.exception.BookingValidationException;
import io.github.wojtekolo.hotelsystem.booking.exception.RoomStayErrorCode;
import io.github.wojtekolo.hotelsystem.booking.model.Booking;
import io.github.wojtekolo.hotelsystem.booking.model.BookingStatus;
import io.github.wojtekolo.hotelsystem.booking.model.RoomStay;
import io.github.wojtekolo.hotelsystem.common.exceptions.*;
import io.github.wojtekolo.hotelsystem.customer.model.Customer;
import io.github.wojtekolo.hotelsystem.customer.persistence.CustomerRepository;
import io.github.wojtekolo.hotelsystem.employee.model.Employee;
import io.github.wojtekolo.hotelsystem.employee.persistence.EmployeeRepository;
import io.github.wojtekolo.hotelsystem.room.model.Room;
import io.github.wojtekolo.hotelsystem.room.persistence.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingMapper bookingMapper;
    private final EmployeeRepository employeeRepository;
    private final RoomRepository roomRepository;
    private final CustomerRepository customerRepository;
    private final BookingRepository bookingRepository;
    private final BookingValidator bookingValidator;

    @Transactional
    public BookingDetails addBooking(BookingCreateRequest request) {
        request.stays().stream()
               .map(RoomStayCreateRequest::roomId)
               .distinct()
               .sorted()
               .forEach(roomRepository::findByIdWithLock);

        Employee employee = findEmployee(request.employeeId());
        Customer customer = findCustomer(request.customerId());
        Booking booking = Booking.createDefault(customer, employee);

        addInitialStays(booking, request.stays(), employee);
        List<InternalRoomStayConflict> internalConflicts = bookingValidator.validateInternalConflicts(booking.getStays());
        List<ExternalRoomStayConflict> externalConflicts = bookingValidator.validateExternalConflicts(booking.getStays());

        if (!internalConflicts.isEmpty() || !externalConflicts.isEmpty())
            throw new BookingValidationException("Error adding booking", externalConflicts, internalConflicts, new ArrayList<>());

        booking = bookingRepository.save(booking);

        return bookingMapper.toBookingDetails(booking);
    }

    @Transactional
    public BookingDetails updateBooking(Long bookingId, BookingUpdateRequest request) {
        Booking booking = findBooking(bookingId);
        if (booking.getStatus() == BookingStatus.CANCELLED)
            throw new BookingStatusException("Cannot edit cancelled booking", BookingErrorCode.BOOKING_CANCELLED_CANNOT_BE_EDITED, booking.getId());

        Set<Long> roomsToLock = new HashSet<>();

        request.stays().stream()
               .map(RoomStayUpdateRequest::roomId)
               .distinct()
               .sorted()
               .forEach(roomsToLock::add);

        booking.getStays().stream()
               .map(RoomStay::getRoom)
               .map(Room::getId)
               .distinct()
               .sorted()
               .forEach(roomsToLock::add);

        roomsToLock.stream().sorted().forEach(roomRepository::findByIdWithLock);


        Employee employee = findEmployee(request.employeeId());

        updateRoomStays(booking, request.stays(), employee);

        return bookingMapper.toBookingDetails(booking);
    }

    @Transactional
    public BookingDetails getBooking(Long bookingId){
        return bookingMapper.toBookingDetails(findBooking(bookingId));
    }

    private void updateRoomStays(Booking booking, List<RoomStayUpdateRequest> newStays, Employee employee) {
        List<RoomStayBadStatusDetails> badStatusDetails = new ArrayList<>();
        badStatusDetails.addAll(deleteStays(booking, newStays));
        badStatusDetails.addAll(updateCurrentStays(booking, newStays));
        addNewStays(booking, newStays, employee);

        List<InternalRoomStayConflict> internalConflicts = bookingValidator.validateInternalConflicts(booking.getStays());
        List<ExternalRoomStayConflict> externalConflicts = bookingValidator.validateExternalConflicts(booking.getStays(), booking.getId());

        if (!badStatusDetails.isEmpty() || !internalConflicts.isEmpty() || !externalConflicts.isEmpty()) {
            throw new BookingValidationException("Error updating booking", externalConflicts, internalConflicts, badStatusDetails);
        }
    }

    private List<RoomStayBadStatusDetails> deleteStays(Booking booking, List<RoomStayUpdateRequest> newStays) {
        List<Long> requestedIds = newStays.stream().map(RoomStayUpdateRequest::id).filter(Objects::nonNull).toList();
        List<RoomStayBadStatusDetails> badStatusDetails = new ArrayList<>();

        booking.getStays().stream()
               .filter(stay -> !requestedIds.contains(stay.getId()))
               .forEach(roomStay -> {
                   if (!roomStay.tryCancel())
                       badStatusDetails.add(new RoomStayBadStatusDetails(
                               roomStay.getId(), roomStay.getStatus(),
                               RoomStayErrorCode.ONLY_PLANNED_STAY_CAN_BE_CANCELLED)
                       );
               });
        return badStatusDetails;
    }

    private List<RoomStayBadStatusDetails> updateCurrentStays(Booking booking, List<RoomStayUpdateRequest> newStays) {
        Map<Long, RoomStay> currentStaysMap = booking.getStays().stream()
                                                     .collect(Collectors.toMap(RoomStay::getId, s -> s));
        List<RoomStayBadStatusDetails> badStatusDetails = new ArrayList<>();

        for (RoomStayUpdateRequest request : newStays) {
            if (request.id() == null) continue;
            RoomStay roomStay = currentStaysMap.get(request.id());
            if (roomStay == null) continue;

            badStatusDetails.add(updateRoom(roomStay, request.roomId()));
            badStatusDetails.add(updateStartDate(roomStay, request.from()));
            badStatusDetails.add(updateEndDate(roomStay, request.to()));
            badStatusDetails.add(updatePricePerNight(roomStay, request.pricePerNight()));
        }
        return badStatusDetails.stream().filter(Objects::nonNull).toList();
    }

    private void addInitialStays(Booking booking, List<RoomStayCreateRequest> stayRequests, Employee employee) {
        stayRequests.forEach(req ->
                createAndAddPlannedStay(booking, employee, req.roomId(), req.from(), req.to(), req.customPricePerNight()));
    }

    private void addNewStays(Booking booking, List<RoomStayUpdateRequest> newStays, Employee employee) {
        List<Long> existingStayIds = booking.getStays().stream().map(RoomStay::getId).toList();

        newStays.stream()
                .filter(req -> !existingStayIds.contains(req.id()))
                .forEach(req ->
                        createAndAddPlannedStay(booking, employee, req.roomId(), req.from(), req.to(), req.pricePerNight())
                );
    }

    private void createAndAddPlannedStay(Booking booking, Employee employee, Long roomId, LocalDate from, LocalDate to, BigDecimal price) {
        booking.addStay(RoomStay.createPlanned(
                booking,
                findRoom(roomId),
                booking.getCustomer().getLoyaltyStatus().getDiscount(),
                employee,
                from,
                to,
                price
        ));
    }


    private RoomStayBadStatusDetails updateRoom(RoomStay roomStay, Long newRoomId) {
        try {
            Room newRoom = roomRepository.findById(newRoomId).orElseThrow(NoSuchElementException::new);
            if (!roomStay.tryUpdateRoom(newRoom)) {
                return new RoomStayBadStatusDetails(roomStay.getId(), roomStay.getStatus(),
                        RoomStayErrorCode.ONLY_PLANNED_STAY_CAN_HAVE_ROOM_EDITED);
            }
        } catch (NoSuchElementException e) {
            return (new RoomStayBadStatusDetails(
                    roomStay.getId(), roomStay.getStatus(), RoomStayErrorCode.ROOM_NOT_FOUND));
        }
        return null;
    }

    private RoomStayBadStatusDetails updateStartDate(RoomStay roomStay, LocalDate newFrom) {
        if (roomStay.getActiveFrom().equals(newFrom)) {
            return null;
        } else if (newFrom.isBefore(LocalDate.now())) {
            return new RoomStayBadStatusDetails(roomStay.getId(), roomStay.getStatus(),
                    RoomStayErrorCode.CANNOT_SET_START_DATE_IN_THE_PAST);
        } else if (!roomStay.tryUpdateActiveFrom(newFrom)) {
            return new RoomStayBadStatusDetails(roomStay.getId(), roomStay.getStatus(),
                    RoomStayErrorCode.ONLY_PLANNED_STAY_CAN_HAVE_START_DATE_EDITED);
        }
        return null;
    }

    private RoomStayBadStatusDetails updateEndDate(RoomStay roomStay, LocalDate newTo) {
        if (roomStay.getActiveTo().equals(newTo)) {
            return null;
        } else if (newTo.isBefore(LocalDate.now())) {
            return new RoomStayBadStatusDetails(roomStay.getId(), roomStay.getStatus(),
                    RoomStayErrorCode.CANNOT_SET_END_DATE_IN_THE_PAST);
        } else if (!roomStay.tryUpdateActiveTo(newTo)) {
            return new RoomStayBadStatusDetails(roomStay.getId(), roomStay.getStatus(),
                    RoomStayErrorCode.ONLY_PLANNED_OR_ACTIVE_STAY_CAN_HAVE_END_DATE_EDITED);
        }
        return null;
    }

    private RoomStayBadStatusDetails updatePricePerNight(RoomStay roomStay, BigDecimal pricePerNight) {
        if (!roomStay.tryEditPrice(pricePerNight)) {
            return new RoomStayBadStatusDetails(roomStay.getId(), roomStay.getStatus(),
                    RoomStayErrorCode.ONLY_PLANNED_STAY_CAN_HAVE_PRICE_EDITED);
        }
        return null;
    }

    private Employee findEmployee(Long id) {
        return employeeRepository.findById(id)
                                 .orElseThrow(() -> new ResourceNotFoundException("Employee with ID " + id + " not found"));
    }

    private Customer findCustomer(Long id) {
        return customerRepository.findById(id)
                                 .orElseThrow(() -> new ResourceNotFoundException("Customer with ID " + id + " not found"));
    }

    private Room findRoom(Long id) {
        return roomRepository.findById(id)
                             .orElseThrow(() -> new ResourceNotFoundException("Room with ID " + id + " not found"));
    }

    private Booking findBooking(Long id) {
        return bookingRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Booking with ID " + id + " not found"));
    }
}
