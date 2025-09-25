package com.example.banking.model;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.UUID;

public class Account {
    private UUID id;
    private UUID customerId;
    private BigDecimal balance;
    private final Deque<Transaction> transactions; // latest at tail

    public Account(UUID id, UUID customerId) {
        this.id = id;
        this.customerId = customerId;
        this.balance = BigDecimal.ZERO;
        this.transactions = new ArrayDeque<>();
    }

    public UUID getId() { return id; }
    public UUID getCustomerId() { return customerId; }
    public BigDecimal getBalance() { return balance; }

    public synchronized void setBalance(BigDecimal balance) { this.balance = balance; }

    public synchronized void addTransaction(Transaction tx) {
        transactions.addLast(tx);
        // Keep only last 100 for storage; API will slice to 10
        while (transactions.size() > 100) {
            transactions.removeFirst();
        }
    }

    public synchronized List<Transaction> getLatestTransactions(int limit) {
        List<Transaction> all = new ArrayList<>(transactions);
        int size = all.size();
        int from = Math.max(0, size - limit);
        return all.subList(from, size);
    }
}
