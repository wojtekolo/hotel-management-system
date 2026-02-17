package io.github.wojtekolo.hotelsystem.customer;

import io.github.wojtekolo.hotelsystem.common.exceptions.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final LoyaltyStatusRepository loyaltyStatusRepository;

    @Transactional
    public CustomerDetails addCustomer(CustomerCreateRequest createRequest){
        LoyaltyStatus loyaltyStatus = loyaltyStatusRepository.findByName(LoyaltyStatusName.BASIC)
                .orElseThrow(() -> new ResourceNotFoundException("Basic loyalty status not found in database"));
        Customer customer = customerMapper.toEntity(createRequest);
        customer.setLoyaltyStatus(loyaltyStatus);
        Customer savedCustomer = customerRepository.save(customer);
        return customerMapper.toDetails(savedCustomer);
    }
}
