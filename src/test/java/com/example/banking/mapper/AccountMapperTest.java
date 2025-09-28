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
                .ibanCountry("DE")
                .ibanNormalized("DE80270925559385021793")
                .ibanDisplay("DE80 2709 2555 9385 0217 93")
                .build();

        AccountResponse dto = mapper.toResponse(domain);

        assertNotNull(dto);
        assertEquals(id, dto.id());
        assertEquals(customerId, dto.customerId());
        assertEquals(0, dto.balance().compareTo(new BigDecimal("100.0000")));
        assertEquals("DE", dto.ibanCountry());
        assertEquals("DE80270925559385021793", dto.ibanNormalized());
        assertEquals("DE80 2709 2555 9385 0217 93", dto.ibanDisplay());
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
        entity.setIbanCountry("DE");
        entity.setIbanNormalized("DE80270925559385021793");
        entity.setIbanDisplay("DE80 2709 2555 9385 0217 93");

        Account domain = mapper.toDomain(entity);

        assertNotNull(domain);
        assertEquals(id, domain.getId());
        assertEquals(customerId, domain.getCustomerId());
        assertEquals(0, domain.getBalance().compareTo(new BigDecimal("42.5000")));
        assertEquals("DE", domain.getIbanCountry());
        assertEquals("DE80270925559385021793", domain.getIbanNormalized());
        assertEquals("DE80 2709 2555 9385 0217 93", domain.getIbanDisplay());
    }
}