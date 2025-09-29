package com.example.banking.mapper;

import com.example.banking.dto.AccountSummary;
import com.example.banking.dto.AccountSummaryResponse;
import com.example.banking.entity.AccountEntity;
import com.example.banking.model.Account;
import com.example.banking.dto.AccountResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AccountMapper {

    // Domain -> DTO
    @Mapping(target = "id", source = "id")
    @Mapping(target = "customerId", source = "customerId")
    @Mapping(target = "balance", source = "balance")
    @Mapping(target = "ibanCountry", source = "ibanCountry")
    @Mapping(target = "ibanNormalized", source = "ibanNormalized")
    @Mapping(target = "ibanDisplay", source = "ibanDisplay")
    AccountResponse toResponse(Account account);

    // Entity -> Domain
    @Mapping(target = "id", source = "id")
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "balance", source = "balance")
    @Mapping(target = "ibanCountry", source = "ibanCountry")
    @Mapping(target = "ibanNormalized", source = "ibanNormalized")
    @Mapping(target = "ibanDisplay", source = "ibanDisplay")
    Account toDomain(AccountEntity entity);

    // Domain -> DTO summary
    @Mapping(target = "id", source = "id")
    @Mapping(target = "balance", source = "balance")
    @Mapping(target = "ibanCountry", source = "ibanCountry")
    @Mapping(target = "ibanNormalized", source = "ibanNormalized")
    @Mapping(target = "ibanDisplay", source = "ibanDisplay")
    AccountSummary toSummary(Account domain);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "balance", source = "balance")
    @Mapping(target = "ibanCountry", source = "ibanCountry")
    @Mapping(target = "ibanNormalized", source = "ibanNormalized")
    @Mapping(target = "ibanDisplay", source = "ibanDisplay")
    AccountSummaryResponse toAccountSummaryResponse(Account domain);
}
