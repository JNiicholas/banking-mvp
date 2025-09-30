package com.example.banking.mapper;

import com.example.banking.entity.AccountEntity;
import com.example.banking.model.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AccountEntityMapper {

    //TODO: Remove explicit mapping
    @Mapping(target = "id", source = "id")
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "balance", source = "balance")
    @Mapping(target = "ibanCountry", source = "ibanCountry")
    @Mapping(target = "ibanNormalized", source = "ibanNormalized")
    @Mapping(target = "ibanDisplay", source = "ibanDisplay")
    Account toDomain(AccountEntity entity);

    //TODO: Remove explicit mapping
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "customer.id", source = "customerId")
    @Mapping(target = "balance", source = "balance")
    @Mapping(target = "ibanCountry", source = "ibanCountry")
    @Mapping(target = "ibanNormalized", source = "ibanNormalized")
    @Mapping(target = "ibanDisplay", source = "ibanDisplay")
    AccountEntity toNewEntity(Account domain);

    //TODO: Remove explicit mapping
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "customer.id", source = "customerId")
    @Mapping(target = "balance", source = "balance")
    @Mapping(target = "ibanCountry", source = "ibanCountry")
    @Mapping(target = "ibanNormalized", source = "ibanNormalized")
    @Mapping(target = "ibanDisplay", source = "ibanDisplay")
    void updateEntity(@org.mapstruct.MappingTarget AccountEntity entity, Account domain);
}
