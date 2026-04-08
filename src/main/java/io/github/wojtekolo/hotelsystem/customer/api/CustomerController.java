package io.github.wojtekolo.hotelsystem.customer.api;

import io.github.wojtekolo.hotelsystem.customer.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<CustomerDetails> addCustomer(@RequestBody @Valid CustomerCreateRequest createRequest){
        return ResponseEntity.ok(customerService.addCustomer(createRequest));
    }
}
