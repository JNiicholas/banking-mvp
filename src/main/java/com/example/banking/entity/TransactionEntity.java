package com.example.banking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_tx_account_ts_desc", columnList = "accountId,timestamp")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TransactionEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID accountId;

    @Column(nullable = false)
    private Instant timestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Type type;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balanceAfter;

    public enum Type { DEPOSIT, WITHDRAW }
}