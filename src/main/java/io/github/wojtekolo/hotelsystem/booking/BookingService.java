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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

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
        Booking booking = createBooking(customer, employee);

        addStays(booking, request.stays(), customer, employee);
        bookingValidator.validateInternalConflicts(booking.getStays());
        booking = bookingRepository.save(booking);

        return bookingMapper.toBookingDetails(booking, calculateTotalBookingCost(booking.getStays()), toRoomStayDetailsList(booking.getStays()));
    }

    @Transactional
    public BookingDetails updateBooking(BookingUpdateRequest request) {
        Booking currentBooking = findBooking(request.bookingId());
        if (currentBooking.getStatus() == BookingStatus.CANCELLED)
            throw new BookingStatusException("Cannot edit cancelled booking", BookingErrorCode.BOOKING_CANCELLED_CANNOT_BE_EDITED, currentBooking.getId());
        Booking updatedBooking = createBooking(currentBooking.getCustomer(), currentBooking.getCreateBy());
        updatedBooking.setId(currentBooking.getId());


        updateRoomStays(currentBooking, updatedBooking, currentBooking.getStays(), request.stays());

        Booking booking = bookingRepository.save(updatedBooking);
        return bookingMapper.toBookingDetails(booking, calculateTotalBookingCost(booking.getStays()), toRoomStayDetailsList(booking.getStays()));
    }

    private void updateRoomStays(Booking currentBooking, Booking newBooking, List<RoomStay> currentStays, List<RoomStayUpdateRequest> newStays) {

        bookingValidator.validateUpdatedRoomStays(newBooking, currentStays, newStays);

        for (RoomStayUpdateRequest request : newStays) {
            RoomStay updatedRoomStay = createRoomStay(newBooking,
                    findRoom(request.roomId()),
                    currentBooking.getCustomer(),
                    currentBooking.getCreateBy(),
                    request.from(),
                    request.to(),
                    request.pricePerNight());

            updatedRoomStay.setId(request.id());
            newBooking.addStay(updatedRoomStay);

            if (request.id() != null) updatedRoomStay.setId(request.id());
        }
    }

    private BigDecimal calculatePricePerNight(Room room, Customer customer, BigDecimal customPricePerNight) {
        if (customPricePerNight == null) {
            return room.getType().getPricePerNight()
                       .multiply(BigDecimal.ONE.subtract(customer.getLoyaltyStatus().getDiscount()));
        } else {
            return customPricePerNight;
        }
    }

    private BigDecimal calculateTotalStayCost(RoomStay stay) {
        long days = ChronoUnit.DAYS.between(stay.getActiveFrom(), stay.getActiveTo());
        return stay.getPricePerNight().multiply(BigDecimal.valueOf(days));
    }

    private BigDecimal calculateTotalBookingCost(List<RoomStay> roomStays) {
        BigDecimal total = BigDecimal.ZERO;
        for (RoomStay roomStay : roomStays) {
            total = total.add(calculateTotalStayCost(roomStay));
        }
        return total;
    }

    private RoomStay createRoomStay(Booking booking, Room room, Customer customer, Employee employee, LocalDate from, LocalDate to, BigDecimal customPricePerNight) {
        return RoomStay.builder()
                       .booking(booking)
                       .room(room)
                       .pricePerNight(calculatePricePerNight(room, customer, customPricePerNight))
                       .activeFrom(from)
                       .activeTo(to)
                       .status(RoomStayStatus.PLANNED)
                       .createBy(employee)
                       .build();
    }

    private Booking createBooking(Customer customer, Employee employee) {
        return Booking.builder()
                      .customer(customer)
                      .createBy(employee)
                      .paymentStatus(PaymentStatus.UNPAID)
                      .status(BookingStatus.PLANNED)
                      .stays(new ArrayList<>())
                      .build();
    }

    private void addStays(Booking booking, List<RoomStayCreateRequest> stayRequests, Customer customer, Employee employee) {
        List<RoomStayConflict> allConflicts = new ArrayList<>();
        for (RoomStayCreateRequest stayRequest : stayRequests) {

            Room room = findRoom(stayRequest.roomId());

            List<RoomStay> conflicts = roomStayRepository.getConflicts(room.getId(), List.of(RoomStayStatus.ACTIVE,
                    RoomStayStatus.PLANNED), stayRequest.from(), stayRequest.to());

            if (!conflicts.isEmpty()) {
                List<RoomStayConflictDetails> details = new ArrayList<>();
                for (RoomStay conflict : conflicts) {
                    details.add(bookingMapper.toRoomStayConflictDetails(conflict));
                }
                allConflicts.add(new RoomStayConflict(room.getId(), room.getName(), details));
            }

            booking.addStay(createRoomStay(booking, room, customer, employee, stayRequest.from(), stayRequest.to(), stayRequest.customPricePerNight()));
        }
        if (!allConflicts.isEmpty()) throw new BookingConflictException(allConflicts);
    }

    private List<RoomStayDetails> toRoomStayDetailsList(List<RoomStay> roomStays) {
        List<RoomStayDetails> result = new ArrayList<>();
        for (RoomStay roomStay : roomStays) {
            result.add(
                    bookingMapper.toRoomStayDetails(roomStay, calculateTotalStayCost(roomStay))
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

    private boolean doStaysOverLap(RoomStay request1, RoomStay request2) {
        return request1.getActiveTo().isAfter(request2.getActiveFrom()) && request1.getActiveFrom()
                                                                                   .isBefore(request2.getActiveTo());
    }
}
