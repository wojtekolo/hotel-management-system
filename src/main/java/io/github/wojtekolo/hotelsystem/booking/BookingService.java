package io.github.wojtekolo.hotelsystem.booking;

import io.github.wojtekolo.hotelsystem.common.exceptions.BookingConflictException;
import io.github.wojtekolo.hotelsystem.common.exceptions.BookingRequestConflictException;
import io.github.wojtekolo.hotelsystem.common.exceptions.ResourceNotFoundException;
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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingMapper bookingMapper;
    private final EmployeeRepository employeeRepository;
    private final RoomRepository roomRepository;
    private final CustomerRepository customerRepository;
    private final RoomStayRepository roomStayRepository;
    private final BookingRepository bookingRepository;

    @Transactional
    public BookingDetails addBooking(BookingCreateRequest request) {

        Employee employee = findEmployee(request.employeeId());
        Customer customer = findCustomer(request.customerId());
        Booking booking = createBooking(customer, employee);

        validateInternalConflicts(request.stays());

        addStays(booking, request.stays(), customer, employee);

        booking = bookingRepository.save(booking);

        return bookingMapper.toBookingDetails(booking, calculateTotalBookingCost(booking.getStays()), toRoomStayDetailsList(booking.getStays()));
    }

    private BigDecimal calculatePricePerNight(Room room, Customer customer, SingleRoomStayRequest roomStayRequest) {
        if (roomStayRequest.customPricePerNight() == null) {
            return room.getType().getPricePerNight()
                    .multiply(BigDecimal.ONE.subtract(customer.getLoyaltyStatus().getDiscount()));
        } else {
            return roomStayRequest.customPricePerNight();
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

    private RoomStay createRoomStay(Booking booking, Room room, Customer customer, SingleRoomStayRequest stayRequest, Employee employee) {
        return RoomStay.builder()
                .booking(booking)
                .room(room)
                .pricePerNight(calculatePricePerNight(room, customer, stayRequest))
                .activeFrom(stayRequest.from())
                .activeTo(stayRequest.to())
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

    private Employee findEmployee(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee with ID " + id + " not found"));
    }

    private Customer findCustomer(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer with ID " + id + " not found"));
    }

    private void addStays(Booking booking, List<SingleRoomStayRequest> stayRequests, Customer customer, Employee employee) {
        List<RoomStayConflict> allConflicts = new ArrayList<>();
        for (SingleRoomStayRequest stayRequest : stayRequests) {

            Room room = roomRepository.findById(stayRequest.roomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Room with ID " + stayRequest.roomId() + " not found"));

            List<RoomStay> conflicts = roomStayRepository.getConflicts(room.getId(), List.of(RoomStayStatus.ACTIVE,
                    RoomStayStatus.PLANNED), stayRequest.from(), stayRequest.to());

            if (!conflicts.isEmpty()) {
                List<RoomStayConflictDetails> details = new ArrayList<>();
                for (RoomStay conflict : conflicts) {
                    details.add(bookingMapper.toRoomStayConflictDetails(conflict));
                }
                allConflicts.add(new RoomStayConflict(room.getId(), room.getName(), details));
            }

            booking.addStay(createRoomStay(booking, room, customer, stayRequest, employee));
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

    private void validateInternalConflicts(List<SingleRoomStayRequest> requests) {
        List<InternalRoomStayConflict> conflicts = new ArrayList<>();

        List<SingleRoomStayRequest> requestsCopy = new ArrayList<>(requests);
        requestsCopy.sort(Comparator
                .comparing(SingleRoomStayRequest::roomId)
                .thenComparing(SingleRoomStayRequest::from)
        );

        for (int i = 0; i < requestsCopy.size() - 1; i++) {
            for(int j = i+1; j<requestsCopy.size(); j++){
                if (!Objects.equals(requestsCopy.get(i).roomId(), requestsCopy.get(j).roomId())) break;

                if (doStaysOverLap(requestsCopy.get(i), requestsCopy.get(j)) && Objects.equals(requestsCopy.get(i).roomId(), requestsCopy.get(j).roomId())) {
                    conflicts.add(new InternalRoomStayConflict(
                            requestsCopy.get(i).roomId(),
                            requestsCopy.get(i).from(),
                            requestsCopy.get(i).to(),
                            requestsCopy.get(j).from(),
                            requestsCopy.get(j).to()
                    ));
                }
            }
        }
        if (!conflicts.isEmpty()) throw new BookingRequestConflictException(conflicts);
    }

    private boolean doStaysOverLap(SingleRoomStayRequest request1, SingleRoomStayRequest request2) {
        return request1.to().isAfter(request2.from()) && request1.from().isBefore(request2.to());
    }
}
