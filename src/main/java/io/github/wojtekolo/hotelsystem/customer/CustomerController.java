package io.github.wojtekolo.hotelsystem.customer;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customer")
public class CustomerController {
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("/add")
    public ResponseEntity<CustomerDetails> addCustomer(@RequestBody @Valid CustomerCreateRequest createRequest){
        System.out.println(createRequest.person());
        System.out.println(createRequest.description());
        return ResponseEntity.ok(customerService.addCustomer(createRequest));
    }
}
