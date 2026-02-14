package io.github.wojtekolo.hotelsystem.customer;

import io.github.wojtekolo.hotelsystem.common.person.PersonRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final PersonRepository personRepository;
    private final CustomerMapper customerMapper;

    public CustomerService(CustomerRepository customerRepository, PersonRepository personRepository, CustomerMapper customerMapper){
        this.customerRepository = customerRepository;
        this.personRepository = personRepository;
        this.customerMapper = customerMapper;
    }

    @Transactional
    public CustomerDetails addCustomer(CustomerCreateRequest createRequest){
        Customer customer = customerMapper.toEntity(createRequest);
        Customer savedCustomer = customerRepository.save(customer);
        return customerMapper.toDetails(savedCustomer);
    }
}
