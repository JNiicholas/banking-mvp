package com.example.banking.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import com.example.banking.model.Account;
import com.example.banking.dto.AccountResponse;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    AccountMapper INSTANCE = Mappers.getMapper(AccountMapper.class);

    AccountResponse toResponse(Account account);
    Account toEntity(AccountResponse response);
}
