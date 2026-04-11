package io.github.wojtekolo.hotelsystem.booking.service.loading;

import io.github.wojtekolo.hotelsystem.booking.api.request.BookingCreateRequest;
import io.github.wojtekolo.hotelsystem.booking.api.request.BookingUpdateRequest;
import io.github.wojtekolo.hotelsystem.booking.exception.details.IntegrityViolationDetails;
import io.github.wojtekolo.hotelsystem.booking.exception.details.IntegrityErrorCode;
import io.github.wojtekolo.hotelsystem.booking.model.entity.Booking;
import io.github.wojtekolo.hotelsystem.booking.persistence.BookingRepository;
import io.github.wojtekolo.hotelsystem.common.exceptions.ResourceNotFoundException;
import io.github.wojtekolo.hotelsystem.customer.model.Customer;
import io.github.wojtekolo.hotelsystem.customer.persistence.CustomerRepository;
import io.github.wojtekolo.hotelsystem.employee.model.Employee;
import io.github.wojtekolo.hotelsystem.employee.persistence.EmployeeRepository;
import io.github.wojtekolo.hotelsystem.room.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class BookingResourceLoader {
    private final RoomService roomService;
    private final EmployeeRepository employeeRepository;
    private final CustomerRepository customerRepository;
    private final BookingRepository bookingRepository;
    private final BookingRequestHelper requestHelper;

    public BookingResources loadForCreate(BookingCreateRequest request) {
        Set<Long> roomIds = requestHelper.collectAllRoomIds(request);
        return loadResources(null, roomIds, request.employeeId(), request.customerId());
    }

    public BookingResources loadForUpdate(Long bookingId, BookingUpdateRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                                           .orElseThrow(() -> new ResourceNotFoundException("Booking with ID " + bookingId + " not found"));

        booking.ensureEditable();

        Set<Long> roomIds = requestHelper.collectAllRoomIds(booking, request);

        return loadResources(booking, roomIds, request.employeeId(), null);
    }


    public Booking loadBooking(Long id) {
        return bookingRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Booking with ID " + id + " not found"));
    }

    private BookingResources loadResources(Booking booking, Set<Long> roomIds, Long employeeId, Long customerId) {
        RoomLoadResult roomLoad = roomService.findAndLockRooms(roomIds);
        Employee employee = employeeId != null ? employeeRepository.findById(employeeId).orElse(null) : null;
        Customer customer = customerId != null ? customerRepository.findById(customerId).orElse(null) : null;

        List<IntegrityViolationDetails> integrityErrors = new ArrayList<>(validateRooms(roomLoad));
        validateEmployee(employee, employeeId).ifPresent(integrityErrors::add);
        validateCustomer(customer, customerId).ifPresent(integrityErrors::add);

        return new BookingResources(booking, roomLoad, employee, customer, integrityErrors);
    }


    private List<IntegrityViolationDetails> validateRooms(RoomLoadResult roomLoadResult) {
        return roomLoadResult.missingIds().stream().map(id -> {
            Map<String, Object> map = new HashMap<>();
            map.put("roomId", id);
            return new IntegrityViolationDetails(IntegrityErrorCode.ROOM_NOT_FOUND, map);
        }).toList();
    }

    private Optional<IntegrityViolationDetails> validateEmployee(Employee employee, Long employeeId) {
        if (employeeId != null && employee == null) {
            Map<String, Object> map = new HashMap<>();
            map.put("employeeId", employeeId);
            return Optional.of(new IntegrityViolationDetails(IntegrityErrorCode.EMPLOYEE_NOT_FOUND, map));
        }
        return Optional.empty();
    }

    private Optional<IntegrityViolationDetails> validateCustomer(Customer customer, Long customerId) {
        if (customerId != null && customer == null) {
            Map<String, Object> map = new HashMap<>();
            map.put("customerId", customerId);
            return Optional.of(new IntegrityViolationDetails(IntegrityErrorCode.CUSTOMER_NOT_FOUND, map));
        }
        return Optional.empty();
    }
}
