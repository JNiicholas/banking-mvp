package com.example.banking.mapper;

import com.example.banking.entity.AccountEntity;
import com.example.banking.model.Account;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AccountEntityMapperTest {

    private final AccountEntityMapper mapper = Mappers.getMapper(AccountEntityMapper.class);

    @Test
    void toDomain_mapsAllFields() {
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        var entity = new AccountEntity();
        entity.setId(id);
        entity.setCustomerId(customerId);
        entity.setBalance(new BigDecimal("123.4500"));
        entity.setVersion(7L);

        Account domain = mapper.toDomain(entity);

        assertNotNull(domain);
        assertEquals(id, domain.getId());
        assertEquals(customerId, domain.getCustomerId());
        assertEquals(0, domain.getBalance().compareTo(new BigDecimal("123.4500")));
    }

    @Test
    void toEntity_mapsAllFields() {
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        var domain = Account.builder()
                .id(id)
                .customerId(customerId)
                .balance(new BigDecimal("0.0000"))
                .build();

        AccountEntity entity = mapper.toEntity(domain);

        assertNotNull(entity);
        assertEquals(id, entity.getId());
        assertEquals(customerId, entity.getCustomerId());
        assertEquals(0, entity.getBalance().compareTo(new BigDecimal("0.0000")));
        // version is managed by JPA; we don’t assert it here
    }
}