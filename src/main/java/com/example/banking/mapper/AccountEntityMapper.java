package com.example.banking.mapper;

import com.example.banking.entity.AccountEntity;
import com.example.banking.model.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AccountEntityMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "customerId", source = "customerId")
    @Mapping(target = "balance", source = "balance")
    Account toDomain(AccountEntity entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "customerId", source = "customerId")
    @Mapping(target = "balance", source = "balance")
    AccountEntity toEntity(Account domain);
}
