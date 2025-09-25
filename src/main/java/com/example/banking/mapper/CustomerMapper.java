package com.example.banking.mapper;

import com.example.banking.Entity.CustomerEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import com.example.banking.model.Customer;
import com.example.banking.dto.CustomerResponse;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    CustomerMapper INSTANCE = Mappers.getMapper(CustomerMapper.class);

    CustomerResponse toResponse(Customer customer);
    Customer toEntity(CustomerResponse response);
    Customer toDomain(CustomerEntity entity);
}
