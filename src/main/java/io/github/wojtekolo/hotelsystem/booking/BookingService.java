package io.github.wojtekolo.hotelsystem.booking;

import io.github.wojtekolo.hotelsystem.common.exceptions.*;
import io.github.wojtekolo.hotelsystem.customer.Customer;
import io.github.wojtekolo.hotelsystem.customer.CustomerRepository;
import io.github.wojtekolo.hotelsystem.employee.Employee;
import io.github.wojtekolo.hotelsystem.employee.EmployeeRepository;
import io.github.wojtekolo.hotelsystem.room.Room;
import io.github.wojtekolo.hotelsystem.room.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingMapper bookingMapper;
    private final EmployeeRepository employeeRepository;
    private final RoomRepository roomRepository;
    private final CustomerRepository customerRepository;
    private final RoomStayRepository roomStayRepository;
    private final BookingRepository bookingRepository;
    private final BookingValidator bookingValidator;

    @Transactional
    public BookingDetails addBooking(BookingCreateRequest request) {
        Employee employee = findEmployee(request.employeeId());
        Customer customer = findCustomer(request.customerId());
        Booking booking = Booking.createDefault(customer, employee);

        addStays(booking, request.stays(), customer, employee);
        List<InternalRoomStayConflict> internalConflicts = bookingValidator.validateInternalConflicts(booking.getStays());
        List<ExternalRoomStayConflict> externalConflicts = bookingValidator.validateExternalConflicts(booking.getStays());

        if (!internalConflicts.isEmpty() || !externalConflicts.isEmpty())
            throw new BookingValidationException("Error updating booking", externalConflicts, internalConflicts, null);

        booking = bookingRepository.save(booking);

        return bookingMapper.toBookingDetails(booking, booking.calculateTotalCost(), toRoomStayDetailsList(booking.getStays()));
    }

    @Transactional
    public BookingDetails updateBooking(BookingUpdateRequest request) {
        Employee employee = findEmployee(request.employeeId());

        Booking booking = findBooking(request.bookingId());
        if (booking.getStatus() == BookingStatus.CANCELLED)
            throw new BookingStatusException("Cannot edit cancelled booking", BookingErrorCode.BOOKING_CANCELLED_CANNOT_BE_EDITED, booking.getId());


        updateRoomStays(booking, request.stays(), employee);


        booking = bookingRepository.save(booking);
        return bookingMapper.toBookingDetails(booking, booking.calculateTotalCost(), toRoomStayDetailsList(booking.getStays()));
    }

    private void updateRoomStays(Booking booking, List<RoomStayUpdateRequest> newStays, Employee employee) {
        List<RoomStayBadStatusDetails> badStatusDetails = new ArrayList<>();
        badStatusDetails.addAll(deleteStays(booking, newStays));
        badStatusDetails.addAll(updateCurrentStays(booking, newStays));
        badStatusDetails.addAll(addNewStays(booking, newStays, employee));

        List<InternalRoomStayConflict> internalConflicts = bookingValidator.validateInternalConflicts(booking.getStays());
        List<ExternalRoomStayConflict> externalConflicts = bookingValidator.validateExternalConflicts(booking.getStays());

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

            try {
                Room newRoom = roomRepository.findById(request.roomId()).orElseThrow(NoSuchElementException::new);
                if (!roomStay.tryUpdateRoom(newRoom)) {
                    badStatusDetails.add(new RoomStayBadStatusDetails(roomStay.getId(), roomStay.getStatus(),
                            RoomStayErrorCode.ONLY_PLANNED_STAY_CAN_HAVE_ROOM_EDITED));
                }
            } catch (NoSuchElementException e) {
                badStatusDetails.add(new RoomStayBadStatusDetails(
                        roomStay.getId(), roomStay.getStatus(), RoomStayErrorCode.ROOM_NOT_FOUND));
//              If room doesn't exist don't apply other changes
                continue;
            }

            if (!roomStay.tryUpdateActiveFrom(request.from())) {
                badStatusDetails.add(new RoomStayBadStatusDetails(roomStay.getId(), roomStay.getStatus(),
                        RoomStayErrorCode.ONLY_PLANNED_STAY_CAN_HAVE_START_DATE_EDITED));
            }
            if (!roomStay.tryUpdateActiveTo(request.to())) {
                badStatusDetails.add(new RoomStayBadStatusDetails(roomStay.getId(), roomStay.getStatus(),
                        RoomStayErrorCode.ONLY_PLANNED_OR_ACTIVE_STAY_CAN_HAVE_END_DATE_EDITED));
            }
            if (!roomStay.tryEditPrice(request.pricePerNight())) {
                badStatusDetails.add(new RoomStayBadStatusDetails(roomStay.getId(), roomStay.getStatus(),
                        RoomStayErrorCode.ONLY_PLANNED_OR_ACTIVE_STAY_CAN_HAVE_END_DATE_EDITED));
            }
        }
        return badStatusDetails;
    }

    private List<RoomStayBadStatusDetails> addNewStays(Booking booking, List<RoomStayUpdateRequest> newStays, Employee employee) {
        List<Long> oldIds = booking.getStays().stream().map(RoomStay::getId).toList();
        List<RoomStayBadStatusDetails> badStatusDetails = new ArrayList<>();

//        For each ID in newStays which does not exist in currentStays, create new planned Stay
        newStays.stream()
                .filter(newStayRequest -> !oldIds.contains(newStayRequest.id()))
                .forEach(newStayRequest -> {
                            booking.addStay(RoomStay.createPlanned(
                                    booking,
                                    findRoom(newStayRequest.roomId()),
                                    booking.getCustomer().getLoyaltyStatus().getDiscount(),
                                    employee,
                                    newStayRequest.from(),
                                    newStayRequest.to(),
                                    newStayRequest.pricePerNight()
                            ));
                        }
                );
        return badStatusDetails;
    }

    private void addStays(Booking booking, List<RoomStayCreateRequest> stayRequests, Customer customer, Employee employee) {
        List<ExternalRoomStayConflict> allConflicts = new ArrayList<>();
        for (RoomStayCreateRequest stayRequest : stayRequests) {

            Room room = findRoom(stayRequest.roomId());

            List<RoomStay> conflicts = roomStayRepository.getConflicts(room.getId(), List.of(RoomStayStatus.ACTIVE,
                    RoomStayStatus.PLANNED), stayRequest.from(), stayRequest.to());

            if (!conflicts.isEmpty()) {
                List<RoomStayConflictDetails> details = new ArrayList<>();
                for (RoomStay conflict : conflicts) {
                    details.add(bookingMapper.toRoomStayConflictDetails(conflict));
                }
                allConflicts.add(new ExternalRoomStayConflict(room.getId(), room.getName(), details));
            }

            booking.addStay(RoomStay.createPlanned(booking, room, customer.getLoyaltyStatus().getDiscount(),
                    employee, stayRequest.from(), stayRequest.to(), stayRequest.customPricePerNight()));
        }
        if (!allConflicts.isEmpty()) throw new BookingConflictException(allConflicts);
    }

    private List<RoomStayDetails> toRoomStayDetailsList(List<RoomStay> roomStays) {
        List<RoomStayDetails> result = new ArrayList<>();
        for (RoomStay roomStay : roomStays) {
            result.add(
                    bookingMapper.toRoomStayDetails(roomStay, roomStay.calculateTotalCost())
            );
        }
        return result;
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
