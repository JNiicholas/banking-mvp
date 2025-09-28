package com.example.banking.mapper;

import com.example.banking.entity.AccountEntity;
import com.example.banking.model.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AccountEntityMapper {

    // entity -> domain (id is fine to expose to the domain)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "balance", source = "balance")
    @Mapping(target = "ibanCountry", source = "ibanCountry")
    @Mapping(target = "ibanNormalized", source = "ibanNormalized")
    @Mapping(target = "ibanDisplay", source = "ibanDisplay")
    Account toDomain(AccountEntity entity);

    // domain -> entity (CREATE): let JPA/DB set id & version
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "customer.id", source = "customerId")
    @Mapping(target = "balance", source = "balance")
    @Mapping(target = "ibanCountry", source = "ibanCountry")
    @Mapping(target = "ibanNormalized", source = "ibanNormalized")
    @Mapping(target = "ibanDisplay", source = "ibanDisplay")
    AccountEntity toNewEntity(Account domain);

    // domain -> entity (UPDATE): copy mutable fields onto a managed entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "customer.id", source = "customerId")
    @Mapping(target = "balance", source = "balance")
    @Mapping(target = "ibanCountry", source = "ibanCountry")
    @Mapping(target = "ibanNormalized", source = "ibanNormalized")
    @Mapping(target = "ibanDisplay", source = "ibanDisplay")
    void updateEntity(@org.mapstruct.MappingTarget AccountEntity entity, Account domain);
}
