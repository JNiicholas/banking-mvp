package com.example.banking.mapper;


import com.example.banking.Entity.CustomerEntity;
import com.example.banking.model.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CustomerEntityMapper {

    // Entity -> Domain
    Customer toDomain(CustomerEntity entity);

    // Domain -> Entity
    CustomerEntity toEntity(Customer domain);
}
