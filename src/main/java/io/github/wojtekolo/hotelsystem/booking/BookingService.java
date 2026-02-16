package io.github.wojtekolo.hotelsystem.booking;

import io.github.wojtekolo.hotelsystem.common.exceptions.BookingConflictException;
import io.github.wojtekolo.hotelsystem.common.exceptions.ResourceNotFoundException;
import io.github.wojtekolo.hotelsystem.customer.Customer;
import io.github.wojtekolo.hotelsystem.customer.CustomerRepository;
import io.github.wojtekolo.hotelsystem.employee.Employee;
import io.github.wojtekolo.hotelsystem.employee.EmployeeRepository;
import io.github.wojtekolo.hotelsystem.room.Room;
import io.github.wojtekolo.hotelsystem.room.RoomRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class BookingService {
    private final BookingMapper bookingMapper;
    private final EmployeeRepository employeeRepository;
    private final RoomRepository roomRepository;
    private final CustomerRepository customerRepository;
    private final RoomStayRepository roomStayRepository;
    private final BookingRepository bookingRepository;

    public BookingService(BookingMapper bookingMapper,
                          EmployeeRepository employeeRepository,
                          RoomRepository roomRepository,
                          CustomerRepository customerRepository,
                          RoomStayRepository roomStayRepository, BookingRepository bookingRepository) {
        this.bookingMapper = bookingMapper;
        this.employeeRepository = employeeRepository;
        this.roomRepository = roomRepository;
        this.customerRepository = customerRepository;
        this.roomStayRepository = roomStayRepository;
        this.bookingRepository = bookingRepository;
    }

    public BookingDetails addBooking(BookingCreateRequest request) {
        Employee employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee with ID " + request.employeeId() + " not found"));

        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer with ID " + request.customerId() + " not found"));

        Booking booking = bookingRepository.save(Booking.builder()
                .customer(customer)
                .createdBy(employee)
                .paymentStatus(PaymentStatus.UNPAID)
                .status(BookingStatus.PLANNED)
                .stays(new ArrayList<>())
                .build());

        List<SingleRoomStayRequest> stayRequests = request.stays();
        List<RoomStay> roomStays = new ArrayList<>();


        for (int i = 0; i < stayRequests.size(); i++) {
            SingleRoomStayRequest stayRequest = stayRequests.get(i);

            Room room = roomRepository.findById(stayRequest.roomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Room with ID " + stayRequest.roomId() + " not found"));

            List<RoomStay> conflicts = roomStayRepository.getConficts(room.getId(), List.of(RoomStayStatus.ACTIVE,
                    RoomStayStatus.PLANNED), stayRequest.from(), stayRequest.to());

            if (!conflicts.isEmpty()) throw new BookingConflictException(conflicts);

            RoomStay stay = RoomStay.builder()
                    .booking(booking)
                    .room(room)
                    .pricePerNight(calculatePricePerNight(room, customer, stayRequest))
                    .activeFrom(stayRequest.from())
                    .activeTo(stayRequest.to())
                    .status(RoomStayStatus.PLANNED)
                    .createdBy(employee)
                    .build();
            booking.addStay(stay);
        }

        Booking savedBooking = bookingRepository.save(booking);


        return new BookingDetails(
                booking.getId(),
                customer.getId(),
                customer.getPerson().getName(),
                customer.getPerson().getSurname(),
                customer.getPrivatePhone(),
                booking.getCreateTime(),
                calculateTotalBookingCost(booking.getStays()),
                booking.getPaymentStatus(),
                booking.getStatus(),
                toRoomStayDetailsList(booking.getStays())
        );
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
