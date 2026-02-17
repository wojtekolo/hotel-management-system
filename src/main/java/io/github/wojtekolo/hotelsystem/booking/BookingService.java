package io.github.wojtekolo.hotelsystem.booking;

import io.github.wojtekolo.hotelsystem.common.exceptions.BookingConflictException;
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
import java.util.List;

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
        for (SingleRoomStayRequest stayRequest : stayRequests) {

            Room room = roomRepository.findById(stayRequest.roomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Room with ID " + stayRequest.roomId() + " not found"));

            List<RoomStay> conflicts = roomStayRepository.getConflicts(room.getId(), List.of(RoomStayStatus.ACTIVE,
                    RoomStayStatus.PLANNED), stayRequest.from(), stayRequest.to());

            if (!conflicts.isEmpty()) throw new BookingConflictException(conflicts);

            booking.addStay(createRoomStay(booking, room, customer, stayRequest, employee));
        }
    }

    private List<RoomStayDetails> toRoomStayDetailsList(List<RoomStay> roomStays) {
        List<RoomStayDetails> result = new ArrayList<>();
        for (int i = 0; i < roomStays.size(); i++) {
            result.add(
                    bookingMapper.toRoomStayDetails(roomStays.get(i), calculateTotalStayCost(roomStays.get(i)))
            );
        }
        return result;
    }
}
