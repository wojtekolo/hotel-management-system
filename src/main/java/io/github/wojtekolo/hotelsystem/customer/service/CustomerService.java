package io.github.wojtekolo.hotelsystem.customer.service;

import io.github.wojtekolo.hotelsystem.common.exceptions.ResourceNotFoundException;
import io.github.wojtekolo.hotelsystem.customer.api.CustomerCreateRequest;
import io.github.wojtekolo.hotelsystem.customer.api.CustomerDetails;
import io.github.wojtekolo.hotelsystem.customer.model.Customer;
import io.github.wojtekolo.hotelsystem.customer.model.LoyaltyStatus;
import io.github.wojtekolo.hotelsystem.customer.model.LoyaltyStatusName;
import io.github.wojtekolo.hotelsystem.customer.persistence.CustomerRepository;
import io.github.wojtekolo.hotelsystem.customer.persistence.LoyaltyStatusRepository;
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
