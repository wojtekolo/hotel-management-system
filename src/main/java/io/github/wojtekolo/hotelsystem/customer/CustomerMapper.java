package io.github.wojtekolo.hotelsystem.customer;

import io.github.wojtekolo.hotelsystem.common.person.PersonMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = PersonMapper.class)
public interface CustomerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "loyaltyStatus", ignore = true)
    Customer toEntity(CustomerCreateRequest createRequest);

    CustomerDetails toDetails(Customer customer);

    default String toCustomerFullName(Customer customer){
        if(customer == null || customer.getPerson()==null) return null;
        return customer.getPerson().getFullName();
    }
}
