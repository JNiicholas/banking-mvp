package com.example.banking.mapper;

import com.example.banking.entity.AccountEntity;
import com.example.banking.entity.CustomerEntity;
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
        var ce = new CustomerEntity();
        ce.setId(customerId);
        entity.setCustomer(ce);
        entity.setBalance(new BigDecimal("123.4500"));
        entity.setVersion(7L);
        entity.setIbanCountry("DE");
        entity.setIbanNormalized("DE80270925559385021793");
        entity.setIbanDisplay("DE80 2709 2555 9385 0217 93");

        Account domain = mapper.toDomain(entity);

        assertNotNull(domain);
        assertEquals(id, domain.getId());
        assertEquals(customerId, domain.getCustomerId());
        assertEquals(0, domain.getBalance().compareTo(new BigDecimal("123.4500")));
        assertEquals("DE", domain.getIbanCountry());
        assertEquals("DE80270925559385021793", domain.getIbanNormalized());
        assertEquals("DE80 2709 2555 9385 0217 93", domain.getIbanDisplay());
    }

    @Test
    void toEntity_mapsAllFields() {
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Account domain = Account.builder()
                .id(id)
                .customerId(customerId)
                .balance(new BigDecimal("100.00"))
                .ibanCountry("DE")
                .ibanNormalized("DE80270925559385021793")
                .ibanDisplay("DE80 2709 2555 9385 0217 93")
                .build();

        AccountEntity entity = mapper.toNewEntity(domain);

        assertNotNull(entity);
        assertEquals(customerId, entity.getCustomer().getId());
        assertEquals(0, entity.getBalance().compareTo(new BigDecimal("100.00")));
        assertEquals("DE", entity.getIbanCountry());
        assertEquals("DE80270925559385021793", entity.getIbanNormalized());
        assertEquals("DE80 2709 2555 9385 0217 93", entity.getIbanDisplay());
        // version is managed by JPA; we don’t assert it here
    }
}