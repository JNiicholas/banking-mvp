package com.example.banking.mapper;

import com.example.banking.dto.AccountResponse;
import com.example.banking.entity.AccountEntity;
import com.example.banking.entity.CustomerEntity;
import com.example.banking.model.Account;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AccountMapperTest {

    private final AccountMapper mapper = Mappers.getMapper(AccountMapper.class);

    @Test
    void toResponse_mapsAllFields() {
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        var domain = Account.builder()
                .id(id)
                .customerId(customerId)
                .balance(new BigDecimal("100.0000"))
                .build();

        AccountResponse dto = mapper.toResponse(domain);

        assertNotNull(dto);
        assertEquals(id, dto.id());
        assertEquals(customerId, dto.customerId());
        assertEquals(0, dto.balance().compareTo(new BigDecimal("100.0000")));
    }

    @Test
    void toDomain_fromEntity_mapsIdCustomerAndBalance() {
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        var entity = new AccountEntity();
        entity.setId(id);
        var customer = new CustomerEntity();
        customer.setId(customerId);
        entity.setCustomer(customer);
        entity.setBalance(new BigDecimal("42.5000"));
        entity.setVersion(3L);

        Account domain = mapper.toDomain(entity);

        assertNotNull(domain);
        assertEquals(id, domain.getId());
        assertEquals(customerId, domain.getCustomerId());
        assertEquals(0, domain.getBalance().compareTo(new BigDecimal("42.5000")));
    }
}