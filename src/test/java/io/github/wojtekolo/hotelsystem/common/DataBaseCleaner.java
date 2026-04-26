package io.github.wojtekolo.hotelsystem.common;

import io.github.wojtekolo.hotelsystem.booking.persistence.BookingRepository;
import io.github.wojtekolo.hotelsystem.booking.persistence.RoomStayRepository;
import io.github.wojtekolo.hotelsystem.customer.persistence.CustomerRepository;
import io.github.wojtekolo.hotelsystem.customer.persistence.LoyaltyStatusRepository;
import io.github.wojtekolo.hotelsystem.employee.persistence.EmployeeRepository;
import io.github.wojtekolo.hotelsystem.person.persistence.PersonRepository;
import io.github.wojtekolo.hotelsystem.room.persistence.RoomRepository;
import io.github.wojtekolo.hotelsystem.room.persistence.RoomTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataBaseCleaner {

    @Autowired
    private RoomStayRepository roomStayRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private LoyaltyStatusRepository loyaltyStatusRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    public void cleanUp() {
        roomStayRepository.deleteAll();
        bookingRepository.deleteAll();
        employeeRepository.deleteAll();
        customerRepository.deleteAll();
        personRepository.deleteAll();
        loyaltyStatusRepository.deleteAll();
        roomRepository.deleteAll();
        roomTypeRepository.deleteAll();
    }
}
