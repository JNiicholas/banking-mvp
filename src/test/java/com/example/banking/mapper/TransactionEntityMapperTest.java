package com.example.banking.mapper;

import com.example.banking.entity.TransactionEntity;
import com.example.banking.model.Transaction;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TransactionEntityMapperTest {

    private final TransactionEntityMapper mapper = Mappers.getMapper(TransactionEntityMapper.class);

    @Test
    void toDomain_mapsAllFields() {
        UUID id = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        Instant now = Instant.now();

        TransactionEntity entity = new TransactionEntity();
        entity.setId(id);
        entity.setAccountId(accountId);
        entity.setAmount(new BigDecimal("99.9900"));
        entity.setTimestamp(now);


        Transaction domain = mapper.toDomain(entity);

        assertNotNull(domain);
        assertEquals(id, domain.getId());
        assertEquals(accountId, domain.getAccountId());
        assertEquals(0, domain.getAmount().compareTo(new BigDecimal("99.9900")));
        assertEquals(now, domain.getTimestamp());

    }

    @Test
    void toEntity_mapsAllFields() {
        UUID id = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        Instant now = Instant.now();

        Transaction domain = Transaction.builder()
                .id(id)
                .accountId(accountId)
                .amount(new BigDecimal("123.4500"))
                .timestamp(now)
                .build();

        TransactionEntity entity = mapper.toEntity(domain);

        assertNotNull(entity);
        assertEquals(accountId, entity.getAccountId());
        assertEquals(0, entity.getAmount().compareTo(new BigDecimal("123.4500")));
        assertEquals(now, entity.getTimestamp());

    }
}