package com.example.banking.mapper;

import com.example.banking.entity.AccountEntity;
import com.example.banking.model.Account;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AccountEntityMapper {

    // Entity -> Domain
    Account toDomain(AccountEntity entity);

    // Domain -> Entity
    AccountEntity toEntity(Account domain);
}
