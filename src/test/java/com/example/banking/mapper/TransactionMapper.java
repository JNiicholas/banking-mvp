package com.example.banking.mapper;

import com.example.banking.dto.TransactionResponse;
import com.example.banking.model.Transaction;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TransactionMapperTest {

    private final TransactionMapper mapper = Mappers.getMapper(TransactionMapper.class);

    @Test
    void toResponse_mapsAllFields() {
        UUID id = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        Instant ts = Instant.parse("2025-01-01T00:00:00Z");
        BigDecimal amount = new BigDecimal("123.4500");
        BigDecimal balanceAfter = new BigDecimal("223.4500");

        Transaction domain = new Transaction(id, accountId, ts, Transaction.Type.DEPOSIT, amount, balanceAfter);

        TransactionResponse dto = mapper.toResponse(domain);

        assertNotNull(dto);
        assertEquals(id, dto.id());
        assertEquals(ts, dto.timestamp());
        assertEquals("DEPOSIT", dto.type());
        assertEquals(0, dto.amount().compareTo(amount));
        assertEquals(0, dto.balanceAfter().compareTo(balanceAfter));
    }

    @Test
    void toDomain_mapsAllFields() {
        UUID id = UUID.randomUUID();
        Instant ts = Instant.parse("2025-02-02T12:34:56Z");
        BigDecimal amount = new BigDecimal("50.00");
        BigDecimal balanceAfter = new BigDecimal("150.0000");

        TransactionResponse dto = new TransactionResponse(id, ts, "WITHDRAW", amount, balanceAfter);

        Transaction domain = mapper.toDomain(dto);

        assertNotNull(domain);
        assertEquals(id, domain.getId());
        assertEquals(ts, domain.getTimestamp());
        assertEquals(Transaction.Type.WITHDRAW, domain.getType());
        assertEquals(0, domain.getAmount().compareTo(amount));
        assertEquals(0, domain.getBalanceAfter().compareTo(balanceAfter));
    }
}
