package com.example.banking.mapper;

import com.example.banking.entity.CustomerEntity;
import com.example.banking.model.Customer;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CustomerEntityMapper {

    @InheritInverseConfiguration(name = "toEntity")
    Customer toDomain(CustomerEntity entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "externalAuthId", source = "externalAuthId")
    @Mapping(target = "externalAuthRealm", source = "externalAuthRealm")
    CustomerEntity toEntity(Customer domain);

}
