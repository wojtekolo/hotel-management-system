package io.github.wojtekolo.hotelsystem.customer;

import io.github.wojtekolo.hotelsystem.common.person.Person;
import io.github.wojtekolo.hotelsystem.common.person.PersonTestUtils;

import java.math.BigDecimal;

public class CustomerTestUtils {
    public static Customer.CustomerBuilder aValidCustomerWithPersonWithLoyalty(Person person, LoyaltyStatus loyaltyStatus){
        return Customer.builder()
                .person(person)
                .description("testDescription")
                .loyaltyStatus(loyaltyStatus)
                .privateEmail("testCustomer@email.com")
                .privatePhone("123456789");
    }
    public static Customer.CustomerBuilder aValidCustomer(){
        return aValidCustomerWithPersonWithLoyalty(PersonTestUtils.aValidPerson().build(), aValidLoyaltyStatus().build());
    }


    public static LoyaltyStatus.LoyaltyStatusBuilder aValidLoyaltyStatus(){
        return LoyaltyStatus.builder()
                .discount(BigDecimal.ZERO)
                .name(LoyaltyStatusName.BASIC);
    }
}
