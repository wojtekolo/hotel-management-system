package io.github.wojtekolo.hotelsystem.customer;

import io.github.wojtekolo.hotelsystem.common.person.Person;
import io.github.wojtekolo.hotelsystem.common.person.PersonTestUtils;
import io.github.wojtekolo.hotelsystem.customer.model.Customer;
import io.github.wojtekolo.hotelsystem.customer.model.LoyaltyStatus;
import io.github.wojtekolo.hotelsystem.customer.model.LoyaltyStatusName;

import java.math.BigDecimal;

public class CustomerTestUtils {
    public static Customer.CustomerBuilder aValidCustomer(Person person, LoyaltyStatus loyaltyStatus){
        return Customer.builder()
                .person(person)
                .description("testDescription")
                .loyaltyStatus(loyaltyStatus)
                .privateEmail("testCustomer@email.com")
                .privatePhone("123456789");
    }
    public static Customer.CustomerBuilder aValidCustomer(){
        return aValidCustomer(PersonTestUtils.aValidPerson().build(), aValidLoyaltyStatus().build());
    }


    public static LoyaltyStatus.LoyaltyStatusBuilder aValidLoyaltyStatus(){
        return LoyaltyStatus.builder()
                .discount(BigDecimal.ZERO)
                .name(LoyaltyStatusName.BASIC);
    }
}
