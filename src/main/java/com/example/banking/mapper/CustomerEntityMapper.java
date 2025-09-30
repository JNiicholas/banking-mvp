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

    //TODO: Remove explicit mapping
    @Mapping(target = "id", source = "id")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "externalAuthId", source = "externalAuthId")
    @Mapping(target = "externalAuthRealm", source = "externalAuthRealm")
    @Mapping(target = "accounts", source = "accounts")
    CustomerEntity toEntity(Customer domain);

}
