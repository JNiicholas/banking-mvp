package com.example.banking.mapper;

import com.example.banking.dto.CustomerResponse;
import com.example.banking.entity.CustomerEntity;
import com.example.banking.model.Customer;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CustomerMapper {
    CustomerMapper INSTANCE = Mappers.getMapper(CustomerMapper.class);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "externalAuthId", source = "externalAuthId")
    @Mapping(target = "externalAuthRealm", source = "externalAuthRealm")
    CustomerResponse toResponse(Customer domain);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "externalAuthId", source = "externalAuthId")
    @Mapping(target = "externalAuthRealm", source = "externalAuthRealm")
    Customer toDomain(CustomerEntity entity);
}
